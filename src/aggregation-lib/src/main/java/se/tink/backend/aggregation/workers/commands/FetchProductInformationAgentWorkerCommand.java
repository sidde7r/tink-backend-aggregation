package se.tink.backend.aggregation.workers.commands;

import com.google.common.collect.ImmutableList;
import se.tink.backend.aggregation.agents.CreateProductExecutor;
import se.tink.backend.aggregation.agents.HttpLoggableExecutor;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.log.HttpLoggingFilterFactory;
import se.tink.backend.aggregation.rpc.CredentialsRequestType;
import se.tink.backend.aggregation.rpc.FakedCredentials;
import se.tink.backend.aggregation.rpc.ProductInformationRequest;
import se.tink.backend.aggregation.utils.CredentialsStringMasker;
import se.tink.backend.aggregation.utils.StringMasker;
import se.tink.backend.aggregation.workers.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.AgentWorkerContext;
import se.tink.backend.aggregation.rpc.Credentials;

public class FetchProductInformationAgentWorkerCommand extends AgentWorkerCommand {
    private static final AggregationLogger log = new AggregationLogger(FetchProductInformationAgentWorkerCommand.class);
    private static final String LOG_TAG_CREATE_PRODUCT_INFORMATION = CredentialsRequestType.PRODUCT_INFORMATION.name();
    private final AgentWorkerContext context;
    private final ProductInformationRequest request;
    private final FakedCredentials fakedCredentials;

    public FetchProductInformationAgentWorkerCommand(AgentWorkerContext context, ProductInformationRequest request) {
        this.context = context;
        this.fakedCredentials = request.getCredentials();
        this.request = request;
    }

    @Override
    public AgentWorkerCommandResult execute() throws Exception {
        CreateProductExecutor createProductExecutor;
        if (context.getAgent() instanceof CreateProductExecutor) {
            createProductExecutor = (CreateProductExecutor) context.getAgent();
        } else {
            log.error("Agent does not support creating product information.");
            return AgentWorkerCommandResult.ABORT;
        }

        HttpLoggingFilterFactory loggingFilterFactory = createLoggingFilterFactory(createProductExecutor.getClass());
        try {
            createProductExecutor.attachHttpFilters(loggingFilterFactory);
            createProductExecutor.fetchProductInformation(
                    request.getProductType(),
                    request.getProductInstanceId(),
                    request.getParameters());

            return AgentWorkerCommandResult.CONTINUE;
        } catch (Exception e) {
            log.error("Could not create product information.", e);
            return AgentWorkerCommandResult.ABORT;
        } finally {
            loggingFilterFactory.removeClientFilters();
        }
    }

    private HttpLoggingFilterFactory createLoggingFilterFactory(Class<? extends HttpLoggableExecutor> agentClass) {
        Iterable<StringMasker> stringMaskers = createHttpLogMaskers(fakedCredentials);
        return new HttpLoggingFilterFactory(log, getHttpLogTag(), stringMaskers, agentClass);
    }

    private String getHttpLogTag() {
        return LOG_TAG_CREATE_PRODUCT_INFORMATION + ":" + request.getProductType().name();
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

    @Override
    public void postProcess() throws Exception {
        // Deliberately left empty.
    }
}
