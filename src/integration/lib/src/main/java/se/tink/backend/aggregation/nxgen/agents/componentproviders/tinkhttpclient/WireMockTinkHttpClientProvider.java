package se.tink.backend.aggregation.nxgen.agents.componentproviders.tinkhttpclient;

import se.tink.backend.aggregation.agents.contexts.CompositeAgentContext;
import se.tink.backend.aggregation.agents.framework.wiremock.configuration.provider.socket.FakeBankSocket;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.logmasker.LogMaskerImpl;
import se.tink.backend.aggregation.nxgen.http.IntegrationWireMockTestTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.NextGenTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.event.configuration.RawBankDataEventCreationStrategies;
import se.tink.backend.aggregation.nxgen.http.event.decision_strategy.RawBankDataEventCreationTriggerStrategy;
import se.tink.backend.aggregation.nxgen.http.event.event_producers.DefaultRawBankDataEventProducer;
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
                                LogMaskerImpl.shouldLog(credentialsRequest.getProvider()))
                        .setAggregatorInfo(context.getAggregatorInfo())
                        .setMetricRegistry(context.getMetricRegistry())
                        .setHttpAapLogger(context.getHttpAapLogger())
                        .setSignatureKeyPair(signatureKeyPair)
                        .setProvider(credentialsRequest.getProvider())
                        .setRawBankDataEventEmissionComponents(
                                new DefaultRawBankDataEventProducer(
                                        RawBankDataEventCreationStrategies
                                                .createDefaultConfiguration()),
                                context.getRawBankDataEventAccumulator(),
                                context.getCorrelationId())
                        .build();

        httpClient.overrideRawBankDataEventCreationTriggerStrategy(
                rawBankDataEventCreationTriggerStrategy);
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
