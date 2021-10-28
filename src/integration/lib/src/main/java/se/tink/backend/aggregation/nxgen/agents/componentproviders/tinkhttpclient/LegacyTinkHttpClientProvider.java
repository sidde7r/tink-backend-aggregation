package se.tink.backend.aggregation.nxgen.agents.componentproviders.tinkhttpclient;

import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.http.LegacyTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

/** @deprecated LegacyTinkHttpClient is to be replaced with NextGenTinkHttpClient. */
@Deprecated
public final class LegacyTinkHttpClientProvider implements TinkHttpClientProvider {

    private final TinkHttpClient tinkHttpClient;

    public LegacyTinkHttpClientProvider(
            final CredentialsRequest credentialsRequest,
            final AgentContext context,
            final SignatureKeyPair signatureKeyPair) {

        tinkHttpClient =
                new LegacyTinkHttpClient(
                        context.getAggregatorInfo(),
                        context.getMetricRegistry(),
                        context.getRawHttpTrafficLogger(),
                        signatureKeyPair,
                        credentialsRequest.getProvider(),
                        context.getLogMasker(),
                        context.getLogMasker().shouldLog(credentialsRequest.getProvider()));
    }

    @Override
    public TinkHttpClient getTinkHttpClient() {
        return tinkHttpClient;
    }
}
