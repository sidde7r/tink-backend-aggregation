package se.tink.backend.aggregation.nxgen.agents.componentproviders.tinkhttpclient.factory;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.tinkhttpclient.TinkHttpClientProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.tinkhttpclient.WireMockTinkHttpClientProvider;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class WireMockTinkHttpClientProviderFactory implements TinkHttpClientProviderFactory {

    private final String wireMockServerHost;

    @Inject
    public WireMockTinkHttpClientProviderFactory(
            @Named("wireMockServerHost") String wireMockServerHost) {
        this.wireMockServerHost = wireMockServerHost;
    }

    @Override
    public TinkHttpClientProvider createTinkHttpClientProvider(
            CredentialsRequest credentialsRequest,
            AgentContext context,
            SignatureKeyPair signatureKeyPair) {

        return new WireMockTinkHttpClientProvider(
                credentialsRequest, context, signatureKeyPair, wireMockServerHost);
    }
}
