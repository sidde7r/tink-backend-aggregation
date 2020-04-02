package se.tink.backend.aggregation.nxgen.agents.componentproviders.tinkhttpclient;

import com.google.inject.Inject;
import java.util.Optional;
import se.tink.backend.aggregation.agents.contexts.CompositeAgentContext;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.logmasker.LogMaskerImpl;
import se.tink.backend.aggregation.nxgen.http.IntegrationWireMockTestTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.NextGenTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class WireMockTinkHttpClientProvider implements TinkHttpClientProvider {

    private final TinkHttpClient tinkHttpClient;

    @Inject
    public WireMockTinkHttpClientProvider(
            final CredentialsRequest credentialsRequest,
            final CompositeAgentContext context,
            final SignatureKeyPair signatureKeyPair,
            final String wireMockServerHost) {

        final TinkHttpClient httpClient =
                NextGenTinkHttpClient.builder(
                                context.getLogMasker(),
                                LogMaskerImpl.shouldLog(credentialsRequest.getProvider()),
                                Optional.ofNullable(context.getConfiguration())
                                        .map(AgentsServiceConfiguration::getTestConfiguration)
                                        .orElse(null))
                        .setAggregatorInfo(context.getAggregatorInfo())
                        .setMetricRegistry(context.getMetricRegistry())
                        .setLogOutputStream(context.getLogOutputStream())
                        .setSignatureKeyPair(signatureKeyPair)
                        .setProvider(credentialsRequest.getProvider())
                        .build();

        httpClient.setCensorSensitiveHeaders(false);
        httpClient.disableSslVerification();

        this.tinkHttpClient =
                new IntegrationWireMockTestTinkHttpClient(httpClient, wireMockServerHost);
    }

    @Override
    public TinkHttpClient getTinkHttpClient() {
        return tinkHttpClient;
    }
}
