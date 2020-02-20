package se.tink.backend.aggregation.nxgen.agents.componentproviders.tinkhttpclient.factory;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.tinkhttpclient.NextGenTinkHttpClientProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.tinkhttpclient.TinkHttpClientProvider;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class NextGenTinkHttpClientProviderFactory implements TinkHttpClientProviderFactory {

    @Override
    public TinkHttpClientProvider createTinkHttpClientProvider(
            CredentialsRequest credentialsRequest,
            AgentContext context,
            SignatureKeyPair signatureKeyPair) {
        return new NextGenTinkHttpClientProvider(credentialsRequest, context, signatureKeyPair);
    }
}
