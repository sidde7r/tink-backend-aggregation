package se.tink.backend.aggregation.nxgen.agents.strategy;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.log.LogMasker;
import se.tink.backend.aggregation.nxgen.http.NextGenTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

/** A strategy for SubsequentGenerationAgent which binds TinkHttpClient to NextGenTinkHttpClient. */
public final class NextGenSubsequentGenerationAgentStrategy
        implements SubsequentGenerationAgentStrategy {

    private final CredentialsRequest credentialsRequest;
    private final AgentContext context;
    private final TinkHttpClient tinkHttpClient;

    NextGenSubsequentGenerationAgentStrategy(
            final CredentialsRequest credentialsRequest,
            final AgentContext context,
            final SignatureKeyPair signatureKeyPair) {
        this.credentialsRequest = credentialsRequest;
        this.context = context;
        tinkHttpClient =
                NextGenTinkHttpClient.builder(
                                context.getLogMasker(),
                                LogMasker.shouldLog(credentialsRequest.getProvider()))
                        .setAggregatorInfo(context.getAggregatorInfo())
                        .setMetricRegistry(context.getMetricRegistry())
                        .setLogOutputStream(context.getLogOutputStream())
                        .setSignatureKeyPair(signatureKeyPair)
                        .setProvider(credentialsRequest.getProvider())
                        .build();
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
