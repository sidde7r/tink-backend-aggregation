package se.tink.backend.aggregation.workers.commands;

import com.google.common.collect.ImmutableList;
import com.sun.jersey.api.client.filter.ClientFilter;
import se.tink.backend.aggregation.agents.CreateProductExecutor;
import se.tink.backend.aggregation.agents.HttpLoggableExecutor;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.log.HttpLoggingFilter;
import se.tink.backend.aggregation.rpc.CredentialsRequestType;
import se.tink.backend.aggregation.rpc.RefreshApplicationRequest;
import se.tink.backend.aggregation.utils.CredentialsStringMasker;
import se.tink.backend.aggregation.utils.StringMasker;
import se.tink.backend.aggregation.workers.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.AgentWorkerContext;
import se.tink.backend.aggregation.rpc.Credentials;

public class RefreshApplicationAgentWorkerCommand extends AgentWorkerCommand {
    private static final AggregationLogger log = new AggregationLogger(RefreshApplicationAgentWorkerCommand.class);
    private static final String LOG_TAG_PRODUCT_REFRESH = CredentialsRequestType.PRODUCT_REFRESH.name();
    private final AgentWorkerContext context;
    private final RefreshApplicationRequest request;
    private final Credentials credentials;

    public RefreshApplicationAgentWorkerCommand(AgentWorkerContext context, RefreshApplicationRequest request) {
        this.context = context;
        this.credentials = request.getCredentials();
        this.request = request;
    }

    @Override
    public AgentWorkerCommandResult execute() throws Exception {
        CreateProductExecutor createProductExecutor;
        if (context.getAgent() instanceof CreateProductExecutor) {
            createProductExecutor = (CreateProductExecutor) context.getAgent();
        } else {
            log.error("Agent does not support refresh product.");
            return AgentWorkerCommandResult.ABORT;
        }

        try {
            createProductExecutor.refreshApplication(
                    request.getProductType(),
                    request.getApplicationId(),
                    request.getParameters());

            return AgentWorkerCommandResult.CONTINUE;
        } catch (Exception e) {
            log.error("Could not refresh application.", e);
            return AgentWorkerCommandResult.ABORT;
        }
    }

    private ClientFilter createHttpLoggingFilter(Class<? extends HttpLoggableExecutor> agentClass) {
        Iterable<StringMasker> stringMaskers = createHttpLogMaskers(credentials);
        return new HttpLoggingFilter(log, getHttpLogTag(), stringMaskers, agentClass);
    }

    private String getHttpLogTag() {
        return LOG_TAG_PRODUCT_REFRESH + ":" + request.getProductType().name();
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
