package se.tink.backend.aggregation.nxgen.agents.componentproviders.tinkhttpclient;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.http.NextGenTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.event.configuration.RawBankDataEventCreationStrategies;
import se.tink.backend.aggregation.nxgen.http.event.event_producers.DefaultRawBankDataEventProducer;
import se.tink.backend.aggregation.nxgen.http.metrics.MetricFilter;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class NextGenTinkHttpClientProvider implements TinkHttpClientProvider {

    private final TinkHttpClient tinkHttpClient;

    @Inject
    public NextGenTinkHttpClientProvider(
            final CredentialsRequest credentialsRequest,
            final AgentContext context,
            final SignatureKeyPair signatureKeyPair) {
        tinkHttpClient =
                NextGenTinkHttpClient.builder(
                                context.getLogMasker(),
                                context.getLogMasker().shouldLog(credentialsRequest.getProvider()))
                        .setAggregatorInfo(context.getAggregatorInfo())
                        .setRawHttpTrafficLogger(context.getRawHttpTrafficLogger())
                        .setJsonHttpTrafficLogger(context.getJsonHttpTrafficLogger())
                        .setSignatureKeyPair(signatureKeyPair)
                        .setProvider(credentialsRequest.getProvider())
                        .setRawBankDataEventEmissionComponents(
                                new DefaultRawBankDataEventProducer(
                                        RawBankDataEventCreationStrategies
                                                .createDefaultConfiguration()),
                                context.getRawBankDataEventAccumulator(),
                                context::getCurrentRefreshableItemInProgress,
                                context.getCorrelationId())
                        .build();

        if (context.getMetricRegistry() != null && credentialsRequest.getProvider() != null) {
            tinkHttpClient.addFilter(
                    new MetricFilter(
                            context.getMetricRegistry(), credentialsRequest.getProvider()));
        }
    }

    @Override
    public TinkHttpClient getTinkHttpClient() {
        return tinkHttpClient;
    }
}
