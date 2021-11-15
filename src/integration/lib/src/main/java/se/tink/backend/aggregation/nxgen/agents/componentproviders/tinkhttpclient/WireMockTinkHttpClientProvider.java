package se.tink.backend.aggregation.nxgen.agents.componentproviders.tinkhttpclient;

import se.tink.backend.aggregation.agents.contexts.CompositeAgentContext;
import se.tink.backend.aggregation.agents.framework.wiremock.configuration.provider.socket.FakeBankSocket;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.http.IntegrationWireMockTestTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.NextGenTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.raw_data_events.configuration.RawBankDataEventCreationStrategies;
import se.tink.backend.aggregation.nxgen.raw_data_events.decision_strategy.AllowAlwaysRawBankDataEventCreationTriggerStrategy;
import se.tink.backend.aggregation.nxgen.raw_data_events.decision_strategy.RawBankDataEventCreationTriggerStrategy;
import se.tink.backend.aggregation.nxgen.raw_data_events.event_producers.DefaultRawBankDataEventProducer;
import se.tink.backend.aggregation.nxgen.raw_data_events.interceptor.RawBankDataEventProducerInterceptor;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class WireMockTinkHttpClientProvider implements TinkHttpClientProvider {

    private final TinkHttpClient tinkHttpClient;

    public WireMockTinkHttpClientProvider(
            final CredentialsRequest credentialsRequest,
            final CompositeAgentContext context,
            final SignatureKeyPair signatureKeyPair,
            final FakeBankSocket fakeBankSocket,
            final RawBankDataEventCreationTriggerStrategy rawBankDataEventCreationTriggerStrategy) {

        final TinkHttpClient httpClient =
                NextGenTinkHttpClient.builder(
                                context.getLogMasker(),
                                context.getLogMasker().shouldLog(credentialsRequest.getProvider()))
                        .setAggregatorInfo(context.getAggregatorInfo())
                        .setRawHttpTrafficLogger(context.getRawHttpTrafficLogger())
                        .setSignatureKeyPair(signatureKeyPair)
                        .setProvider(credentialsRequest.getProvider())
                        .build();

        // Build raw bank data event emission interceptor
        DefaultRawBankDataEventProducer rawBankDataEventProducer =
                new DefaultRawBankDataEventProducer(
                        RawBankDataEventCreationStrategies.createDefaultConfiguration());
        String correlationId = context.getCorrelationId();

        httpClient.addFilter(
                new RawBankDataEventProducerInterceptor(
                        rawBankDataEventProducer,
                        context.getRawBankDataEventAccumulator(),
                        context::getCurrentRefreshableItemInProgress,
                        correlationId,
                        credentialsRequest.getProvider().getName(),
                        new AllowAlwaysRawBankDataEventCreationTriggerStrategy()));

        httpClient.disableSslVerification();

        this.tinkHttpClient =
                new IntegrationWireMockTestTinkHttpClient(
                        httpClient, fakeBankSocket.getHttpsHost());
    }

    @Override
    public TinkHttpClient getTinkHttpClient() {
        return tinkHttpClient;
    }
}
