package se.tink.backend.aggregation.workers.commands;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import java.util.Optional;
import se.tink.backend.aggregation.agents.Agent;
import se.tink.backend.aggregation.agents.HttpLoggableExecutor;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.TransferExecutor;
import se.tink.backend.aggregation.agents.TransferExecutorNxgen;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.http.filter.ClientFilterFactory;
import se.tink.backend.aggregation.nxgen.http.log.HttpLoggingFilterFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.rpc.TransferRequest;
import se.tink.backend.aggregation.utils.CredentialsStringMasker;
import se.tink.backend.aggregation.utils.StringMasker;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.metrics.AgentWorkerCommandMetricState;
import se.tink.backend.aggregation.workers.metrics.MetricAction;
import se.tink.backend.core.signableoperation.SignableOperation;
import se.tink.backend.core.signableoperation.SignableOperationStatuses;
import se.tink.backend.core.transfer.Transfer;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.uuid.UUIDUtils;

public class TransferAgentWorkerCommand extends SignableOperationAgentWorkerCommand implements MetricsCommand {
    private static final AggregationLogger log = new AggregationLogger(TransferAgentWorkerCommand.class);
    private static final String LOG_TAG_TRANSFER = "EXECUTE_TRANSFER";

    private final TransferRequest transferRequest;
    private final AgentWorkerCommandMetricState metrics;

    public TransferAgentWorkerCommand(AgentWorkerCommandContext context, TransferRequest transferRequest,
            AgentWorkerCommandMetricState metrics) {
        super(context, transferRequest.getCredentials(), transferRequest.getSignableOperation());
        this.transferRequest = transferRequest;
        this.metrics = metrics.init(this);
    }

    @Override
    public AgentWorkerCommandResult execute() throws Exception {
        Agent agent = context.getAgent();
        Catalog catalog = context.getCatalog();

        Transfer transfer = transferRequest.getTransfer();
        SignableOperation signableOperation = transferRequest.getSignableOperation();

        signableOperation.setStatus(SignableOperationStatuses.EXECUTING);
        context.updateSignableOperation(signableOperation);

        if (!(agent instanceof TransferExecutor) && !(agent instanceof TransferExecutorNxgen)) {
            log.error("Agent does not support executing transfers");
            return AgentWorkerCommandResult.ABORT;
        }

        log.info(transfer, "Executing transfer.");
        MetricAction metricAction = metrics.buildAction(
                new MetricId.MetricLabels()
                        .add("action", transferRequest.isUpdate() ? MetricName.UPDATE_TRANSFER : MetricName.EXECUTE_TRANSFER));

        HttpLoggableExecutor httpLoggableExecutor = (HttpLoggableExecutor)agent;
        ClientFilterFactory loggingFilterFactory = createHttpLoggingFilterFactory(
                getLogTagTransfer(transfer), httpLoggableExecutor.getClass(), credentials);

        Optional<String> operationStatusMessage = Optional.empty();
        try {
            // We want to explicitly log everything that has to do with transfers.
            httpLoggableExecutor.attachHttpFilters(loggingFilterFactory);
            log.info(transfer, getTransferExecuteLogInfo(transfer, transferRequest.isUpdate()));

            if (agent instanceof TransferExecutor) {
                TransferExecutor transferExecutor = (TransferExecutor) agent;
                if (transferRequest.isUpdate()) {
                    transferExecutor.update(transfer);
                } else {
                    transferExecutor.execute(transfer);
                }
            } else if (agent instanceof TransferExecutorNxgen) {
                TransferExecutorNxgen transferExecutorNxgen = (TransferExecutorNxgen) agent;
                if (transferRequest.isUpdate()) {
                    transferExecutorNxgen.update(transfer);
                } else {
                    operationStatusMessage = transferExecutorNxgen.execute(transfer);
                }
            }

            metricAction.completed();
            if (operationStatusMessage.isPresent()) {
                context.updateSignableOperationStatus(signableOperation, SignableOperationStatuses.EXECUTED, operationStatusMessage.get());
            } else {
                context.updateSignableOperationStatus(signableOperation, SignableOperationStatuses.EXECUTED);
            }
            return AgentWorkerCommandResult.CONTINUE;

        } catch (TransferExecutionException e) {
            // Catching this exception here means that the Credentials will not get status TEMPORARY_ERROR.

            if (Objects.equal(e.getSignableOperationStatus(), SignableOperationStatuses.CANCELLED)) {
                // Skipping logging the exception, e, here because that will log stacktrace which we will alert on
                // and register on dashboard as an error.
                metricAction.cancelled();
                log.info(transfer, "Could not execute transfer.");
            } else {
                metricAction.failed();
                log.error(transfer, "Could not execute transfer.", e);
            }

            signableOperation.setStatus(e.getSignableOperationStatus());
            signableOperation.setStatusMessage(e.getUserMessage());
            context.updateSignableOperation(signableOperation);

            return AgentWorkerCommandResult.ABORT;

        } catch (BankIdException e) {
            switch (e.getError()) {
            case CANCELLED:
            case TIMEOUT:
            case ALREADY_IN_PROGRESS:
            case NO_CLIENT:
            case AUTHORIZATION_REQUIRED: // TODO: This should be a regular AuthorizationException
                metricAction.cancelled();
                log.info(transfer, e.getMessage());
                signableOperation.setStatus(SignableOperationStatuses.CANCELLED);
                break;
            default:
                metricAction.failed();
                log.error(transfer, String.format("Caught unexpected %s", e.getMessage()), e);
                signableOperation.setStatus(SignableOperationStatuses.FAILED);
            }

            signableOperation.setStatusMessage(catalog.getString(e.getUserMessage()));
            context.updateSignableOperation(signableOperation);

            return AgentWorkerCommandResult.ABORT;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            metricAction.failed();
            log.error(transfer, "Could not execute transfer.", e);

            signableOperation.setStatus(SignableOperationStatuses.FAILED);
            signableOperation.setStatusMessage(catalog.getString("Something went wrong."));
            context.updateSignableOperation(signableOperation);

            return AgentWorkerCommandResult.ABORT;

        } catch (Exception e) {
            // Catching this exception here means that the Credentials will not get status TEMPORARY_ERROR.
            metricAction.failed();
            log.error(transfer, "Could not execute transfer.", e);

            signableOperation.setStatus(SignableOperationStatuses.FAILED);
            signableOperation.setStatusMessage(catalog.getString("Something went wrong."));
            context.updateSignableOperation(signableOperation);

            return AgentWorkerCommandResult.ABORT;
        } finally {
            // Disable the logging filter when we're done with the transfer execute command.
            loggingFilterFactory.removeClientFilters();
            resetCredentialsStatus();
        }
    }

    private static ClientFilterFactory createHttpLoggingFilterFactory(String logTag,
            Class<? extends HttpLoggableExecutor> agentClass, Credentials credentials) {
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

    private static String getLogTagTransfer(Transfer transfer) {
        return LOG_TAG_TRANSFER + ":" + UUIDUtils.toTinkUUID(transfer.getId());
    }

    @Override
    public void postProcess() throws Exception {
        // Deliberately left empty.
    }

    private String getTransferExecuteLogInfo(Transfer transfer, boolean isUpdate) {
        switch (transfer.getType()) {
        case EINVOICE:
            if (isUpdate) {
                return "Approving e-invoice.";
            }
            break;
        case BANK_TRANSFER:
            if (!isUpdate) {
                return "Creating a new bank transfer.";
            }
            break;
        case PAYMENT:
            if (isUpdate) {
                return "Updating an upcoming payment.";
            } else {
                return "Creating a new payment.";
            }
        }
        return "Unrecognized transfer command.";
    }

    private static class MetricName {
        private static final String METRIC = "agent_transfer";

        private static final String UPDATE_TRANSFER = "update";
        private static final String EXECUTE_TRANSFER = "execute";
    }

    @Override
    public String getMetricName() {
        return MetricName.METRIC;
    }
}
