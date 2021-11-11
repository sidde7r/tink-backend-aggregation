package se.tink.backend.aggregation.nxgen.agents.componentproviders.tinkhttpclient;

import com.google.inject.Inject;
import java.util.Objects;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.http.NextGenTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.event.configuration.RawBankDataEventCreationStrategies;
import se.tink.backend.aggregation.nxgen.http.event.decision_strategy.RandomStickyDecisionMakerRawBankDataEventCreationTriggerStrategy;
import se.tink.backend.aggregation.nxgen.http.event.event_producers.DefaultRawBankDataEventProducer;
import se.tink.backend.aggregation.nxgen.http.event.event_producers.RawBankDataEventAccumulator;
import se.tink.backend.aggregation.nxgen.http.event.interceptor.RawBankDataEventProducerInterceptor;
import se.tink.backend.aggregation.nxgen.http_metrics.MetricFilter;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.RefreshableItem;

@Slf4j
public final class NextGenTinkHttpClientProvider implements TinkHttpClientProvider {

    private final TinkHttpClient tinkHttpClient;
    // Determines the percentage of operations for which we will emit raw bank data event for
    // all HTTP traffic
    private static final double RAW_BANK_DATA_EVENT_EMISSION_RATE = 0;

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
                        .build();

        if (context.getMetricRegistry() != null && credentialsRequest.getProvider() != null) {
            tinkHttpClient.addFilter(
                    new MetricFilter(
                            context.getMetricRegistry(), credentialsRequest.getProvider()));
        }

        // Build raw bank data event emission interceptor
        Supplier<RefreshableItem> refreshableItemSupplier =
                context::getCurrentRefreshableItemInProgress;
        DefaultRawBankDataEventProducer rawBankDataEventProducer =
                new DefaultRawBankDataEventProducer(
                        RawBankDataEventCreationStrategies.createDefaultConfiguration());
        RawBankDataEventAccumulator rawBankDataEventAccumulator =
                context.getRawBankDataEventAccumulator();
        String correlationId = context.getCorrelationId();
        if (Objects.nonNull(correlationId)) {

            Provider provider = credentialsRequest.getProvider();

            double emissionRate = RAW_BANK_DATA_EVENT_EMISSION_RATE;

            /* --> HERE ONE CAN ADD EMISSION RATE OVERRIDES PER PROVIDER <-- */

            if (provider.getMarket() == "GB"
                    && Provider.AccessType.OPEN_BANKING.equals(provider.getAccessType())) {
                emissionRate = 0.01;
            }

            /* --> OVERRIDES END <-- */

            tinkHttpClient.addFilter(
                    new RawBankDataEventProducerInterceptor(
                            rawBankDataEventProducer,
                            rawBankDataEventAccumulator,
                            refreshableItemSupplier,
                            correlationId,
                            provider.getName(),
                            new RandomStickyDecisionMakerRawBankDataEventCreationTriggerStrategy(
                                    emissionRate)));
        }
    }

    @Override
    public TinkHttpClient getTinkHttpClient() {
        return tinkHttpClient;
    }
}
