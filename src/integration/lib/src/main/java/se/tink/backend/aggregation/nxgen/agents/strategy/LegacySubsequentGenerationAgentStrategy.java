package se.tink.backend.aggregation.nxgen.agents.strategy;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.log.LogMasker;
import se.tink.backend.aggregation.nxgen.http.LegacyTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

/**
 * A strategy for SubsequentGenerationAgent which binds TinkHttpClient to LegacyTinkHttpClient.
 *
 * @deprecated It is recommended to make your agent use NextGenTinkHttpClient instead.
 */
@Deprecated
public final class LegacySubsequentGenerationAgentStrategy
        implements SubsequentGenerationAgentStrategy {

    private final CredentialsRequest credentialsRequest;
    private final AgentContext context;
    private final TinkHttpClient tinkHttpClient;

    LegacySubsequentGenerationAgentStrategy(
            final CredentialsRequest credentialsRequest,
            final AgentContext context,
            final SignatureKeyPair signatureKeyPair) {
        this.credentialsRequest = credentialsRequest;
        this.context = context;
        tinkHttpClient =
                new LegacyTinkHttpClient(
                        context.getAggregatorInfo(),
                        context.getMetricRegistry(),
                        context.getLogOutputStream(),
                        signatureKeyPair,
                        credentialsRequest.getProvider(),
                        context.getLogMasker(),
                        LogMasker.shouldLog(credentialsRequest.getProvider()));
    }

    @Override
    public TinkHttpClient getTinkHttpClient() {
        return tinkHttpClient;
    }

    @Override
    public CredentialsRequest getCredentialsRequest() {
        return credentialsRequest;
    }

    @Override
    public AgentContext getContext() {
        return context;
    }
}
