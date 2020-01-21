package se.tink.backend.aggregation.nxgen.agents.strategy;

import java.util.Optional;
import se.tink.backend.aggregation.agents.CompositeAgentContext;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.logmasker.LogMasker;
import se.tink.backend.aggregation.nxgen.http.NextGenTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

/** A strategy for SubsequentGenerationAgent which binds TinkHttpClient to NextGenTinkHttpClient. */
public final class NextGenSubsequentGenerationAgentStrategy
        implements SubsequentGenerationAgentStrategy {

    private final CredentialsRequest credentialsRequest;
    private final CompositeAgentContext context;
    private final TinkHttpClient tinkHttpClient;
    private final SuperAbstractAgentStrategy superAbstractAgentStrategy;

    NextGenSubsequentGenerationAgentStrategy(
            final CredentialsRequest credentialsRequest,
            final CompositeAgentContext context,
            final SignatureKeyPair signatureKeyPair) {
        this.credentialsRequest = credentialsRequest;
        this.context = context;
        tinkHttpClient =
                NextGenTinkHttpClient.builder(
                                context.getLogMasker(),
                                LogMasker.shouldLog(credentialsRequest.getProvider()),
                                Optional.ofNullable(context.getConfiguration())
                                        .map(AgentsServiceConfiguration::getTestConfiguration)
                                        .orElse(null))
                        .setAggregatorInfo(context.getAggregatorInfo())
                        .setMetricRegistry(context.getMetricRegistry())
                        .setLogOutputStream(context.getLogOutputStream())
                        .setSignatureKeyPair(signatureKeyPair)
                        .setProvider(credentialsRequest.getProvider())
                        .build();
        superAbstractAgentStrategy =
                new DefaultSuperAbstractAgentStrategy(credentialsRequest, context);
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
    public CompositeAgentContext getContext() {
        return context;
    }

    @Override
    public SuperAbstractAgentStrategy getSuperAbstractAgentStrategy() {
        return superAbstractAgentStrategy;
    }
}
