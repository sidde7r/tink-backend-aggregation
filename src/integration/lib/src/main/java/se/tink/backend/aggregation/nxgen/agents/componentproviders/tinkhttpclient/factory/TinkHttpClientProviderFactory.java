package se.tink.backend.aggregation.nxgen.agents.componentproviders.tinkhttpclient.factory;

import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.tinkhttpclient.TinkHttpClientProvider;
import se.tink.libraries.credentials.service.CredentialsRequest;

public interface TinkHttpClientProviderFactory {

    TinkHttpClientProvider createTinkHttpClientProvider(
            final CredentialsRequest credentialsRequest,
            final AgentContext context,
            final SignatureKeyPair signatureKeyPair);
}
