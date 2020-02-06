package se.tink.backend.aggregation.nxgen.agents.componentproviders.tinkhttpclient;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.logmasker.LogMasker;
import se.tink.backend.aggregation.nxgen.http.NextGenTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class NextGenTinkHttpClientProvider implements TinkHttpClientProvider {

    private final TinkHttpClient tinkHttpClient;

    public NextGenTinkHttpClientProvider(
            final CredentialsRequest credentialsRequest,
            final AgentContext context,
            final SignatureKeyPair signatureKeyPair) {

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
    }

    @Override
    public TinkHttpClient getTinkHttpClient() {
        return tinkHttpClient;
    }
}
