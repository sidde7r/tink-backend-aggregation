package se.tink.backend.aggregation.nxgen.agents.componentproviders.tinkhttpclient;

import com.google.inject.Inject;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.api.AggregatorInfo;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.http.DefaultResponseStatusHandler;
import se.tink.backend.aggregation.nxgen.http.NextGenTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http_metrics.MetricFilter;
import se.tink.backend.aggregation.nxgen.raw_data_events.configuration.RawBankDataEventCreationStrategies;
import se.tink.backend.aggregation.nxgen.raw_data_events.decision_strategy.RandomStickyDecisionMakerRawBankDataEventCreationTriggerStrategy;
import se.tink.backend.aggregation.nxgen.raw_data_events.event_producers.DefaultRawBankDataEventProducer;
import se.tink.backend.aggregation.nxgen.raw_data_events.event_producers.RawBankDataEventAccumulator;
import se.tink.backend.aggregation.nxgen.raw_data_events.interceptor.RawBankDataEventProducerInterceptor;
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

        String aggregatorIdentifier =
                Optional.ofNullable(context.getAggregatorInfo())
                        .map(AggregatorInfo::getAggregatorIdentifier)
                        .orElse(null);

        tinkHttpClient =
                NextGenTinkHttpClient.builder(
                                context.getLogMasker(),
                                context.getLogMasker().shouldLog(credentialsRequest.getProvider()))
                        .setAggregatorIdentifier(aggregatorIdentifier)
                        .setRawHttpTrafficLogger(context.getRawHttpTrafficLogger())
                        .setJsonHttpTrafficLogger(context.getJsonHttpTrafficLogger())
                        .setSignatureKeyPair(signatureKeyPair)
                        .build();

        Optional.ofNullable(credentialsRequest.getProvider())
                .map(Provider::getName)
                .map(DefaultResponseStatusHandler::new)
                .ifPresent(tinkHttpClient::setResponseStatusHandler);

        if (context.getMetricRegistry() != null) {
            tinkHttpClient.addFilter(
                    new MetricFilter(
                            context.getMetricRegistry(), credentialsRequest.getProvider()));
        }

        if (context.getCorrelationId() != null) {
            tinkHttpClient.addFilter(
                    getRawBankDataEventFilter(
                            context.getCorrelationId(),
                            credentialsRequest.getProvider(),
                            context::getCurrentRefreshableItemInProgress,
                            context.getRawBankDataEventAccumulator()));
        }
    }

    private Filter getRawBankDataEventFilter(
            String correlationId,
            Provider provider,
            Supplier<RefreshableItem> refreshableItemSupplier,
            RawBankDataEventAccumulator rawBankDataEventAccumulator) {
        // Build raw bank data event emission interceptor
        DefaultRawBankDataEventProducer rawBankDataEventProducer =
                new DefaultRawBankDataEventProducer(
                        RawBankDataEventCreationStrategies.createDefaultConfiguration());

        double emissionRate = RAW_BANK_DATA_EVENT_EMISSION_RATE;

        /* --> HERE ONE CAN ADD EMISSION RATE OVERRIDES PER PROVIDER <-- */

        if (provider.getMarket() == "GB"
                && Provider.AccessType.OPEN_BANKING.equals(provider.getAccessType())) {
            emissionRate = 0.01;
        }

        /* --> OVERRIDES END <-- */

        return new RawBankDataEventProducerInterceptor(
                rawBankDataEventProducer,
                rawBankDataEventAccumulator,
                refreshableItemSupplier,
                correlationId,
                provider.getName(),
                new RandomStickyDecisionMakerRawBankDataEventCreationTriggerStrategy(emissionRate));
    }

    @Override
    public TinkHttpClient getTinkHttpClient() {
        return tinkHttpClient;
    }
}
