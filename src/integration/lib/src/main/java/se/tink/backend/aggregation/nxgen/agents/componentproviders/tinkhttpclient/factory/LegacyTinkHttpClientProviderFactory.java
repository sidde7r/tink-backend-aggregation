package se.tink.backend.aggregation.nxgen.agents.componentproviders.tinkhttpclient.factory;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.tinkhttpclient.LegacyTinkHttpClientProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.tinkhttpclient.TinkHttpClientProvider;
import se.tink.libraries.credentials.service.CredentialsRequest;

/** @deprecated LegacyTinkHttpClient is to be replaced with NextGenTinkHttpClient. */
@Deprecated
public final class LegacyTinkHttpClientProviderFactory implements TinkHttpClientProviderFactory {

    @Override
    public TinkHttpClientProvider createTinkHttpClientProvider(
            CredentialsRequest credentialsRequest,
            AgentContext context,
            SignatureKeyPair signatureKeyPair) {
        return new LegacyTinkHttpClientProvider(credentialsRequest, context, signatureKeyPair);
    }
}
