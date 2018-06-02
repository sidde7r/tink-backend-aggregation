package se.tink.backend.aggregation.workers.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import java.util.Locale;
import se.tink.backend.aggregation.agents.Agent;
import se.tink.backend.aggregation.agents.CreateProductExecutor;
import se.tink.backend.aggregation.agents.HttpLoggableExecutor;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.application.InvalidApplicationException;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.log.HttpLoggingFilterFactory;
import se.tink.backend.aggregation.rpc.CreateProductRequest;
import se.tink.backend.aggregation.rpc.CreateProductResponse;
import se.tink.backend.aggregation.utils.CredentialsStringMasker;
import se.tink.backend.aggregation.utils.StringMasker;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.AgentWorkerContext;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.uuid.UUIDUtils;
import se.tink.libraries.application.GenericApplication;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.core.signableoperation.SignableOperation;
import se.tink.backend.core.transfer.SignableOperationStatuses;

public class CreateProductAgentWorkerCommand extends SignableOperationAgentWorkerCommand {

    private static final AggregationLogger log = new AggregationLogger(CreateProductAgentWorkerCommand.class);
    private static final String LOG_TAG_CREATE_PRODUCT = "CREATE PRODUCT";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private CreateProductRequest createProductRequest;
    private final MetricRegistry metricRegistry;
    private final MetricId createProductMetric;

    public CreateProductAgentWorkerCommand(AgentWorkerContext context, CreateProductRequest createProductRequest) {
        super(context, createProductRequest.getCredentials(), createProductRequest.getSignableOperation());
        this.createProductRequest = createProductRequest;
        this.metricRegistry = context.getMetricRegistry();
        this.createProductMetric = getCreateProductMetric(createProductRequest);
    }

    private static MetricId getCreateProductMetric(CreateProductRequest createProductRequest) {
        return MetricId.newId("create_product")
                .label("provider", createProductRequest.getProvider().getName())
                .label("type", createProductRequest.getApplication().getType().name());
    }

    @Override
    public AgentWorkerCommandResult execute() throws Exception {
        Agent agent = context.getAgent();
        Catalog catalog = context.getCatalog();

        GenericApplication application = createProductRequest.getApplication();
        SignableOperation signableOperation = createProductRequest.getSignableOperation();

        signableOperation.setStatus(SignableOperationStatuses.EXECUTING);
        context.updateSignableOperation(signableOperation);

        if (agent instanceof CreateProductExecutor) {

            log.info(application, "Creating new product: " + MAPPER.writeValueAsString(application));

            CreateProductExecutor createProductExecutor = (CreateProductExecutor) agent;
            HttpLoggingFilterFactory loggingFilterFactory = createHttpLoggingFilterFactory(getLogTag(application),
                    createProductExecutor.getClass(), credentials);

            try {
                // We want to explicitly log everything that has to do with creating new products.
                createProductExecutor.attachHttpFilters(loggingFilterFactory);

                // Create the new product and set the external reference as the signable object.
                CreateProductResponse response = createProductExecutor.create(application);
                signableOperation.setSignableObject(response.getExternalId());
                context.updateSignableOperationStatus(signableOperation, SignableOperationStatuses.EXECUTED);

                metricRegistry.meter(createProductMetric.label("outcome", "success")).inc();

                log.info(application, "Successfully created new product.");
                return AgentWorkerCommandResult.CONTINUE;
            } catch (BankIdException e) {
                switch (e.getError()) {
                case CANCELLED:
                case TIMEOUT:
                case ALREADY_IN_PROGRESS:
                case NO_CLIENT:
                    log.info(application, e.getMessage());
                    signableOperation.setStatus(SignableOperationStatuses.CANCELLED);
                    signableOperation.setStatusDetailsKey(SignableOperation.StatusDetailsKey.BANKID_FAILED);
                    break;
                case USER_VALIDATION_ERROR:
                    // This will e.g. happen when user has too new BankID -> SEB doesn't accept the applicant
                    log.warn("User validation failed", e);
                    signableOperation.setStatus(SignableOperationStatuses.FAILED);
                    signableOperation.setStatusDetailsKey(SignableOperation.StatusDetailsKey.USER_VALIDATION_ERROR);
                    break;
                default:
                    log.error(String.format("Caught unexpected %s", e.getMessage()), e);
                    signableOperation.setStatus(SignableOperationStatuses.FAILED);
                    signableOperation.setStatusDetailsKey(SignableOperation.StatusDetailsKey.BANKID_FAILED);
                }

                signableOperation.setStatusMessage(catalog.getString(e.getUserMessage()));
                context.updateSignableOperation(signableOperation);

                metricRegistry.meter(createProductMetric.label("outcome", getOutcomeLabel(signableOperation))).inc();
                return AgentWorkerCommandResult.ABORT;
            } catch (InvalidApplicationException e) {
                log.error(String.format("Application(%s) validation failed: %s",
                        application.getType(), e.getMessage()), e);

                signableOperation.setStatus(SignableOperationStatuses.FAILED);
                signableOperation.setStatusDetailsKey(SignableOperation.StatusDetailsKey.INVALID_INPUT);
                signableOperation.setStatusMessage(e.getMessage());
                context.updateSignableOperation(signableOperation);

                metricRegistry.meter(createProductMetric.label("outcome", getOutcomeLabel(signableOperation))).inc();
                return AgentWorkerCommandResult.ABORT;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();

                log.error(application, "Could not create new product.", e);

                signableOperation.setStatus(SignableOperationStatuses.FAILED);
                signableOperation.setStatusDetailsKey(SignableOperation.StatusDetailsKey.TECHNICAL_ERROR);
                signableOperation.setStatusMessage(catalog.getString("Something went wrong."));
                context.updateSignableOperation(signableOperation);

                metricRegistry.meter(createProductMetric.label("outcome", getOutcomeLabel(signableOperation))).inc();
                return AgentWorkerCommandResult.ABORT;
            } catch (Exception e) {
                // Catching this exception here means that the Credentials will not get status TEMPORARY_ERROR.

                log.error(application, "Could not create new product.", e);

                signableOperation.setStatus(SignableOperationStatuses.FAILED);
                signableOperation.setStatusDetailsKey(SignableOperation.StatusDetailsKey.TECHNICAL_ERROR);
                signableOperation.setStatusMessage(catalog.getString("Something went wrong."));
                context.updateSignableOperation(signableOperation);

                metricRegistry.meter(createProductMetric.label("outcome", getOutcomeLabel(signableOperation))).inc();
                return AgentWorkerCommandResult.ABORT;
            } finally {
                // Disable the logging and the create product filter when we're done with the execute command.
                loggingFilterFactory.removeClientFilters();
                resetCredentialsStatus();
            }
        } else {
            log.error("Agent does not support creating new products.");
            metricRegistry.meter(createProductMetric.label("outcome", "not_implemented")).inc();
            return AgentWorkerCommandResult.ABORT;
        }
    }

    private static String getOutcomeLabel(SignableOperation signableOperation) {
        return signableOperation.getStatusDetailsKey().name().toLowerCase(Locale.ENGLISH);
    }

    private static HttpLoggingFilterFactory createHttpLoggingFilterFactory(String logTag,
            Class<? extends HttpLoggableExecutor> agentClass,
            Credentials credentials) {
        Iterable<StringMasker> stringMaskers = createHttpLogMaskers(credentials);
        return new HttpLoggingFilterFactory(log, logTag, stringMaskers, agentClass);
    }

    private static Iterable<StringMasker> createHttpLogMaskers(Credentials credentials) {
        StringMasker stringMasker = new CredentialsStringMasker(credentials,
                ImmutableList.of(
                        CredentialsStringMasker.CredentialsProperty.PASSWORD,
                        CredentialsStringMasker.CredentialsProperty.SECRET_KEY,
                        CredentialsStringMasker.CredentialsProperty.SENSITIVE_PAYLOAD,
                        CredentialsStringMasker.CredentialsProperty.USERNAME));

        return ImmutableList.of(stringMasker);
    }

    private static String getLogTag(GenericApplication application) {
        return LOG_TAG_CREATE_PRODUCT + ":" + UUIDUtils.toTinkUUID(application.getApplicationId());
    }

    @Override
    public void postProcess() throws Exception {
        // Deliberately left empty.
    }

}
