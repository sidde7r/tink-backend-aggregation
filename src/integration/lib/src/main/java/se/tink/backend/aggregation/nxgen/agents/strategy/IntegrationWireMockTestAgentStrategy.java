package se.tink.backend.aggregation.nxgen.agents.strategy;

import java.util.Optional;
import se.tink.backend.aggregation.agents.CompositeAgentContext;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.logmasker.LogMasker;
import se.tink.backend.aggregation.nxgen.http.IntegrationWireMockTestTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.NextGenTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class IntegrationWireMockTestAgentStrategy implements SubsequentGenerationAgentStrategy {

    private final TinkHttpClient tinkHttpClient;
    private final CredentialsRequest credentialsRequest;
    private final CompositeAgentContext context;
    private final SuperAbstractAgentStrategy superAbstractAgentStrategy;

    public IntegrationWireMockTestAgentStrategy(
            final CredentialsRequest credentialsRequest,
            final CompositeAgentContext context,
            final SignatureKeyPair signatureKeyPair,
            final String wireMockServerHost) {

        this.credentialsRequest = credentialsRequest;
        this.context = context;

        final TinkHttpClient httpClient =
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

        httpClient.setDebugOutput(true);
        httpClient.setCensorSensitiveHeaders(false);
        httpClient.disableSslVerification();

        this.tinkHttpClient =
                new IntegrationWireMockTestTinkHttpClient(httpClient, wireMockServerHost);

        this.superAbstractAgentStrategy =
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
