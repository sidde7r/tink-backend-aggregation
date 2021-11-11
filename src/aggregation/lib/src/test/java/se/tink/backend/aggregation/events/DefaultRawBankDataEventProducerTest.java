package se.tink.backend.aggregation.events;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.google.protobuf.AbstractMessage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.agents.rpc.Provider.AccessType;
import se.tink.backend.aggregation.api.AggregatorInfo;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.logmasker.LogMasker;
import se.tink.backend.aggregation.logmasker.LogMasker.LoggingMode;
import se.tink.backend.aggregation.nxgen.http.NextGenTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.event.configuration.RawBankDataEventCreationStrategies;
import se.tink.backend.aggregation.nxgen.http.event.decision_strategy.AllowAlwaysRawBankDataEventCreationTriggerStrategy;
import se.tink.backend.aggregation.nxgen.http.event.decision_strategy.DenyAlwaysRawBankDataEventCreationTriggerStrategy;
import se.tink.backend.aggregation.nxgen.http.event.event_producers.DefaultRawBankDataEventProducer;
import se.tink.backend.aggregation.nxgen.http.event.event_producers.RawBankDataEventAccumulator;
import se.tink.backend.aggregation.nxgen.http.event.event_producers.RawBankDataEventProducer;
import se.tink.backend.aggregation.nxgen.http.event.interceptor.RawBankDataEventProducerInterceptor;
import se.tink.backend.aggregation.nxgen.http.log.executor.raw.RawHttpTrafficLogger;
import se.tink.eventproducerservice.events.grpc.RawBankDataTrackerEventProto;
import se.tink.eventproducerservice.events.grpc.RawBankDataTrackerEventProto.RawBankDataTrackerEvent;
import se.tink.eventproducerservice.events.grpc.RawBankDataTrackerEventProto.RawBankDataTrackerEventBankField;
import se.tink.eventproducerservice.events.grpc.RawBankDataTrackerEventProto.RawBankDataTrackerEventBankFieldType;
import se.tink.libraries.credentials.service.RefreshableItem;

public class DefaultRawBankDataEventProducerTest {

    @Test
    public void whenGivenAProperJsonResponseBodyEventProducerShouldEmitEvent() {

        // given
        DefaultRawBankDataEventProducer defaultRawBankDataEventProducer =
                new DefaultRawBankDataEventProducer(
                        RawBankDataEventCreationStrategies.createDefaultConfiguration());
        String givenResponseBody = "{\"key\": \"value\"}";

        // when
        Optional<RawBankDataTrackerEvent> event =
                defaultRawBankDataEventProducer.produceRawBankDataEvent(
                        givenResponseBody, "dummy-correlationId", "dummy-providerName");

        // then
        Assert.assertTrue(event.isPresent());
    }

    @Test
    public void whenGivenANonJsonResponseBodyEventProducerShouldNotEmitEvent() {

        // given
        DefaultRawBankDataEventProducer defaultRawBankDataEventProducer =
                new DefaultRawBankDataEventProducer(
                        RawBankDataEventCreationStrategies.createDefaultConfiguration());
        String givenResponseBody = "non json data";

        // when
        Optional<RawBankDataTrackerEvent> event =
                defaultRawBankDataEventProducer.produceRawBankDataEvent(
                        givenResponseBody, "dummy-correlationId", "dummy-providerName");

        // then
        Assert.assertFalse(event.isPresent());
    }

    @Test
    public void whenGivenAProperComplexJsonResponseBodyEventProducerShouldEmitEvent() {

        // given
        String responseBodyFilePath =
                "src/aggregation/lib/src/test/java/se/tink/backend/aggregation/events/resources/test1.json";
        String givenResponseBody = readResource(responseBodyFilePath);
        DefaultRawBankDataEventProducer defaultRawBankDataEventProducer =
                new DefaultRawBankDataEventProducer(
                        RawBankDataEventCreationStrategies.createDefaultConfiguration());

        List<RawBankDataTrackerEventProto.RawBankDataTrackerEventBankField> expectedFields =
                new ArrayList<>();
        expectedFields.add(
                RawBankDataTrackerEventBankField.newBuilder()
                        .setFieldPath("dummy_value")
                        .setFieldType(RawBankDataTrackerEventBankFieldType.STRING)
                        .setIsFieldSet(true)
                        .setIsFieldMasked(true)
                        .setFieldValue("LLLL")
                        .setOffset("0")
                        .build());
        expectedFields.add(
                RawBankDataTrackerEventBankField.newBuilder()
                        .setFieldPath("weird.field")
                        .setFieldType(RawBankDataTrackerEventBankFieldType.STRING)
                        .setIsFieldSet(true)
                        .setIsFieldMasked(true)
                        .setFieldValue("LLL")
                        .setOffset("0")
                        .build());
        expectedFields.add(
                RawBankDataTrackerEventBankField.newBuilder()
                        .setFieldPath("dummy_null_value")
                        .setFieldType(RawBankDataTrackerEventBankFieldType.UNKNOWN)
                        .setIsFieldSet(false)
                        .setIsFieldMasked(true)
                        .setFieldValue("MASKED")
                        .setOffset("0")
                        .build());
        expectedFields.add(
                RawBankDataTrackerEventBankField.newBuilder()
                        .setFieldPath("status")
                        .setFieldType(RawBankDataTrackerEventBankFieldType.STRING)
                        .setIsFieldSet(true)
                        .setIsFieldMasked(false)
                        .setFieldValue("NOT_POSSIBLE")
                        .setOffset("0")
                        .build());
        expectedFields.add(
                RawBankDataTrackerEventBankField.newBuilder()
                        .setFieldPath("iban")
                        .setFieldType(RawBankDataTrackerEventBankFieldType.IBAN)
                        .setIsFieldSet(true)
                        .setIsFieldMasked(true)
                        .setFieldValue("LLddddddddddddddddddddddddd")
                        .setOffset("0")
                        .build());
        expectedFields.add(
                RawBankDataTrackerEventBankField.newBuilder()
                        .setFieldPath("bban")
                        .setFieldType(RawBankDataTrackerEventBankFieldType.BBAN)
                        .setIsFieldSet(true)
                        .setIsFieldMasked(true)
                        .setFieldValue("dddddddddddddddddddd")
                        .setOffset("0")
                        .build());
        expectedFields.add(
                RawBankDataTrackerEventBankField.newBuilder()
                        .setFieldPath("transactions.booked[].transactionId")
                        .setFieldType(RawBankDataTrackerEventBankFieldType.STRING)
                        .setIsFieldSet(true)
                        .setIsFieldMasked(true)
                        .setFieldValue("LLLL")
                        .setOffset("0,0")
                        .build());
        expectedFields.add(
                RawBankDataTrackerEventBankField.newBuilder()
                        .setFieldPath("transactions.booked[].valueDate")
                        .setFieldType(RawBankDataTrackerEventBankFieldType.DATE)
                        .setIsFieldSet(true)
                        .setIsFieldMasked(false)
                        .setFieldValue("2021-05-18")
                        .setOffset("0,0")
                        .build());
        expectedFields.add(
                RawBankDataTrackerEventBankField.newBuilder()
                        .setFieldPath("transactions.booked[].bookingDate")
                        .setFieldType(RawBankDataTrackerEventBankFieldType.DATE)
                        .setIsFieldSet(true)
                        .setIsFieldMasked(false)
                        .setFieldValue("2021-05-18")
                        .setOffset("0,0")
                        .build());
        expectedFields.add(
                RawBankDataTrackerEventBankField.newBuilder()
                        .setFieldPath("transactions.booked[].weird.field")
                        .setFieldType(RawBankDataTrackerEventBankFieldType.STRING)
                        .setIsFieldSet(true)
                        .setIsFieldMasked(true)
                        .setFieldValue("LLL")
                        .setOffset("0,0")
                        .build());
        expectedFields.add(
                RawBankDataTrackerEventBankField.newBuilder()
                        .setFieldPath("transactions.booked[].weird[]Field")
                        .setFieldType(RawBankDataTrackerEventBankFieldType.STRING)
                        .setIsFieldSet(true)
                        .setIsFieldMasked(true)
                        .setFieldValue("LLL")
                        .setOffset("0,0")
                        .build());
        expectedFields.add(
                RawBankDataTrackerEventBankField.newBuilder()
                        .setFieldPath("transactions.booked[].transactionAmount.amount")
                        .setFieldType(RawBankDataTrackerEventBankFieldType.DOUBLE)
                        .setIsFieldSet(true)
                        .setIsFieldMasked(false)
                        .setFieldValue("-645.06")
                        .setOffset("0,0")
                        .build());
        expectedFields.add(
                RawBankDataTrackerEventBankField.newBuilder()
                        .setFieldPath("transactions.booked[].transactionAmount.currency")
                        .setFieldType(RawBankDataTrackerEventBankFieldType.STRING)
                        .setIsFieldSet(true)
                        .setIsFieldMasked(false)
                        .setFieldValue("SEK")
                        .setOffset("0,0")
                        .build());
        expectedFields.add(
                RawBankDataTrackerEventBankField.newBuilder()
                        .setFieldPath("transactions.booked[]._links.transactionDetails[].href")
                        .setFieldType(RawBankDataTrackerEventBankFieldType.STRING)
                        .setIsFieldSet(true)
                        .setIsFieldMasked(true)
                        .setFieldValue("LLLLd")
                        .setOffset("0,0,0")
                        .build());
        expectedFields.add(
                RawBankDataTrackerEventBankField.newBuilder()
                        .setFieldPath("transactions.booked[]._links.transactionDetails[].href")
                        .setFieldType(RawBankDataTrackerEventBankFieldType.STRING)
                        .setIsFieldSet(true)
                        .setIsFieldMasked(true)
                        .setFieldValue("LLLLd")
                        .setOffset("0,0,1")
                        .build());
        expectedFields.add(
                RawBankDataTrackerEventBankField.newBuilder()
                        .setFieldPath("transactions.booked[].transactionId")
                        .setFieldType(RawBankDataTrackerEventBankFieldType.STRING)
                        .setIsFieldSet(true)
                        .setIsFieldMasked(true)
                        .setFieldValue("LLd")
                        .setOffset("0,1")
                        .build());
        expectedFields.add(
                RawBankDataTrackerEventBankField.newBuilder()
                        .setFieldPath("transactions.booked[].valueDate")
                        .setFieldType(RawBankDataTrackerEventBankFieldType.DATE)
                        .setIsFieldSet(true)
                        .setIsFieldMasked(false)
                        .setFieldValue("2021-05-18")
                        .setOffset("0,1")
                        .build());
        expectedFields.add(
                RawBankDataTrackerEventBankField.newBuilder()
                        .setFieldPath("transactions.booked[].bookingDate")
                        .setFieldType(RawBankDataTrackerEventBankFieldType.DATE)
                        .setIsFieldSet(true)
                        .setIsFieldMasked(false)
                        .setFieldValue("2021-05-18")
                        .setOffset("0,1")
                        .build());
        expectedFields.add(
                RawBankDataTrackerEventBankField.newBuilder()
                        .setFieldPath("transactions.booked[].transactionAmount.amount")
                        .setFieldType(RawBankDataTrackerEventBankFieldType.DOUBLE)
                        .setIsFieldSet(true)
                        .setIsFieldMasked(false)
                        .setFieldValue("-500.0")
                        .setOffset("0,1")
                        .build());
        expectedFields.add(
                RawBankDataTrackerEventBankField.newBuilder()
                        .setFieldPath("transactions.booked[].transactionAmount.currency")
                        .setFieldType(RawBankDataTrackerEventBankFieldType.STRING)
                        .setIsFieldSet(true)
                        .setIsFieldMasked(false)
                        .setFieldValue("SEK")
                        .setOffset("0,1")
                        .build());
        expectedFields.add(
                RawBankDataTrackerEventBankField.newBuilder()
                        .setFieldPath("transactions.booked[]._links.transactionDetails.href")
                        .setFieldType(RawBankDataTrackerEventBankFieldType.STRING)
                        .setIsFieldSet(true)
                        .setIsFieldMasked(true)
                        .setFieldValue("LLLLd")
                        .setOffset("0,1")
                        .build());
        expectedFields.add(
                RawBankDataTrackerEventBankField.newBuilder()
                        .setFieldPath("transactions.pending[].transactionAmount.amount")
                        .setFieldType(RawBankDataTrackerEventBankFieldType.DOUBLE)
                        .setIsFieldSet(true)
                        .setIsFieldMasked(false)
                        .setFieldValue("830.0")
                        .setOffset("0,0")
                        .build());
        expectedFields.add(
                RawBankDataTrackerEventBankField.newBuilder()
                        .setFieldPath("transactions.pending[].transactionAmount.currency")
                        .setFieldType(RawBankDataTrackerEventBankFieldType.STRING)
                        .setIsFieldSet(true)
                        .setIsFieldMasked(false)
                        .setFieldValue("SEK")
                        .setOffset("0,0")
                        .build());
        expectedFields.add(
                RawBankDataTrackerEventBankField.newBuilder()
                        .setFieldPath("transactions.pending[].valueDate")
                        .setFieldType(RawBankDataTrackerEventBankFieldType.DATE)
                        .setIsFieldSet(true)
                        .setIsFieldMasked(false)
                        .setFieldValue("2021-05-18")
                        .setOffset("0,0")
                        .build());
        expectedFields.add(
                RawBankDataTrackerEventBankField.newBuilder()
                        .setFieldPath("transactions.pending[].descriptiveText")
                        .setFieldType(RawBankDataTrackerEventBankFieldType.STRING)
                        .setIsFieldSet(true)
                        .setIsFieldMasked(true)
                        .setFieldValue("LLLLL LLL")
                        .setOffset("0,0")
                        .build());
        expectedFields.add(
                RawBankDataTrackerEventBankField.newBuilder()
                        .setFieldPath("transactions.pending[].pendingType")
                        .setFieldType(RawBankDataTrackerEventBankFieldType.STRING)
                        .setIsFieldSet(true)
                        .setIsFieldMasked(true)
                        .setFieldValue("LLLLLLLL")
                        .setOffset("0,0")
                        .build());
        expectedFields.add(
                RawBankDataTrackerEventBankField.newBuilder()
                        .setFieldPath("_links.account.href")
                        .setFieldType(RawBankDataTrackerEventBankFieldType.STRING)
                        .setIsFieldSet(true)
                        .setIsFieldMasked(true)
                        .setFieldValue("LLLLd")
                        .setOffset("0")
                        .build());

        // when
        Optional<RawBankDataTrackerEvent> event =
                defaultRawBankDataEventProducer.produceRawBankDataEvent(
                        givenResponseBody, "dummy-correlationId", "dummy-providerName");

        // then
        Assert.assertTrue(event.isPresent());

        List<RawBankDataTrackerEventProto.RawBankDataTrackerEventBankField> actualFields =
                event.get().getFieldDataList();
        assertExpectedAndActualRawBankDataTrackerEventBankFieldsAreTheSame(
                expectedFields, actualFields);
    }

    @Test
    public void
            whenGivenAProperComplexJsonResponseBodyEventProducerShouldEmitEventWithHttpClient() {
        // given
        Provider provider = mock(Provider.class);
        when(provider.getAccessType()).thenReturn(AccessType.OPEN_BANKING);

        RawBankDataEventProducer eventProducer =
                new DefaultRawBankDataEventProducer(
                        RawBankDataEventCreationStrategies.createDefaultConfiguration());

        RawBankDataEventAccumulator eventAccumulator = new RawBankDataEventAccumulator();

        String givenCorrelationId = "test-correlationId";
        NextGenTinkHttpClient client =
                NextGenTinkHttpClient.builder(
                                mock(LogMasker.class), LoggingMode.LOGGING_MASKER_COVERS_SECRETS)
                        .setAggregatorInfo(AggregatorInfo.getAggregatorForTesting())
                        .setRawHttpTrafficLogger(mock(RawHttpTrafficLogger.class))
                        .setSignatureKeyPair(new SignatureKeyPair())
                        .setProvider(provider)
                        .build();

        client.addFilter(
                new RawBankDataEventProducerInterceptor(
                        eventProducer,
                        eventAccumulator,
                        () -> RefreshableItem.CHECKING_ACCOUNTS,
                        givenCorrelationId,
                        "providerName",
                        new AllowAlwaysRawBankDataEventCreationTriggerStrategy()));

        WireMockConfiguration config = wireMockConfig().dynamicPort().dynamicPort();
        WireMockServer wireMockServer = new WireMockServer(config);
        wireMockServer.start();

        MappingBuilder mappingBuilder1 = WireMock.get(WireMock.urlPathEqualTo("/get_accounts"));
        ResponseDefinitionBuilder response1 = WireMock.aResponse();
        String responseBody1 = "{\"keyA\": \"value1\", \"keyB\": \"value2\"}";
        response1.withBody(responseBody1);
        mappingBuilder1.willReturn(response1);
        wireMockServer.stubFor(mappingBuilder1);

        MappingBuilder mappingBuilder2 = WireMock.get(WireMock.urlPathEqualTo("/get_transactions"));
        ResponseDefinitionBuilder response2 = WireMock.aResponse();
        String responseBody2 = "{\"keyC\": \"value3\", \"keyD\": \"value4\"}";
        response2.withBody(responseBody2);
        mappingBuilder2.willReturn(response2);
        wireMockServer.stubFor(mappingBuilder2);

        MappingBuilder mappingBuilder3 = WireMock.get(WireMock.urlPathEqualTo("/get_balances"));
        ResponseDefinitionBuilder response3 = WireMock.aResponse();
        String responseBody3 = "non-json-data";
        response3.withBody(responseBody3);
        mappingBuilder3.willReturn(response3);
        wireMockServer.stubFor(mappingBuilder3);

        List<RawBankDataTrackerEventProto.RawBankDataTrackerEventBankField> expectedFields1 =
                new ArrayList<>();
        List<RawBankDataTrackerEventProto.RawBankDataTrackerEventBankField> expectedFields2 =
                new ArrayList<>();

        expectedFields1.add(
                RawBankDataTrackerEventBankField.newBuilder()
                        .setFieldPath("keyA")
                        .setFieldType(RawBankDataTrackerEventBankFieldType.STRING)
                        .setIsFieldSet(true)
                        .setIsFieldMasked(true)
                        .setFieldValue("LLLLLd")
                        .setOffset("0")
                        .setCount(1)
                        .build());
        expectedFields1.add(
                RawBankDataTrackerEventBankField.newBuilder()
                        .setFieldPath("keyB")
                        .setFieldType(RawBankDataTrackerEventBankFieldType.STRING)
                        .setIsFieldSet(true)
                        .setIsFieldMasked(true)
                        .setFieldValue("LLLLLd")
                        .setOffset("0")
                        .setCount(1)
                        .build());

        expectedFields2.add(
                RawBankDataTrackerEventBankField.newBuilder()
                        .setFieldPath("keyC")
                        .setFieldType(RawBankDataTrackerEventBankFieldType.STRING)
                        .setIsFieldSet(true)
                        .setIsFieldMasked(true)
                        .setFieldValue("LLLLLd")
                        .setOffset("0")
                        .setCount(1)
                        .build());
        expectedFields2.add(
                RawBankDataTrackerEventBankField.newBuilder()
                        .setFieldPath("keyD")
                        .setFieldType(RawBankDataTrackerEventBankFieldType.STRING)
                        .setIsFieldSet(true)
                        .setIsFieldMasked(true)
                        .setFieldValue("LLLLLd")
                        .setOffset("0")
                        .setCount(1)
                        .build());

        // when
        String actualResponse1 =
                client.request(
                                String.format(
                                        "http://localhost:%d/get_accounts", wireMockServer.port()))
                        .get(String.class);
        String actualResponse2 =
                client.request(
                                String.format(
                                        "http://localhost:%d/get_transactions",
                                        wireMockServer.port()))
                        .get(String.class);
        String actualResponse3 =
                client.request(
                                String.format(
                                        "http://localhost:%d/get_balances", wireMockServer.port()))
                        .get(String.class);
        wireMockServer.stop();

        // then
        Assert.assertEquals(responseBody1, actualResponse1);
        Assert.assertEquals(responseBody2, actualResponse2);
        Assert.assertEquals(responseBody3, actualResponse3);
        Assert.assertEquals(2, eventAccumulator.getEventList().size());
        List<RawBankDataTrackerEventProto.RawBankDataTrackerEventBankField> actualFields1 =
                eventAccumulator.getEventList().get(0).getFieldDataList();
        List<RawBankDataTrackerEventProto.RawBankDataTrackerEventBankField> actualFields2 =
                eventAccumulator.getEventList().get(1).getFieldDataList();
        Assert.assertEquals(
                givenCorrelationId, eventAccumulator.getEventList().get(0).getCorrelationId());
        Assert.assertEquals(
                givenCorrelationId, eventAccumulator.getEventList().get(1).getCorrelationId());
        assertExpectedAndActualRawBankDataTrackerEventBankFieldsAreTheSame(
                expectedFields1, actualFields1);
        assertExpectedAndActualRawBankDataTrackerEventBankFieldsAreTheSame(
                expectedFields2, actualFields2);
    }

    @Test
    public void
            whenUsingDefaultDecisionStrategyThatDeniesEventsProducerShouldNotEmitEventWithHttpClient() {
        // given
        Provider provider = mock(Provider.class);
        when(provider.getAccessType()).thenReturn(AccessType.OPEN_BANKING);

        RawBankDataEventProducer eventProducer =
                new DefaultRawBankDataEventProducer(
                        RawBankDataEventCreationStrategies.createDefaultConfiguration());
        RawBankDataEventAccumulator eventAccumulator = new RawBankDataEventAccumulator();

        String givenCorrelationId = "test-correlationId";
        NextGenTinkHttpClient client =
                NextGenTinkHttpClient.builder(
                                mock(LogMasker.class), LoggingMode.LOGGING_MASKER_COVERS_SECRETS)
                        .setAggregatorInfo(AggregatorInfo.getAggregatorForTesting())
                        .setRawHttpTrafficLogger(mock(RawHttpTrafficLogger.class))
                        .setSignatureKeyPair(new SignatureKeyPair())
                        .setProvider(provider)
                        .build();

        client.addFilter(
                new RawBankDataEventProducerInterceptor(
                        eventProducer,
                        eventAccumulator,
                        () -> RefreshableItem.CHECKING_ACCOUNTS,
                        givenCorrelationId,
                        "providerName",
                        new DenyAlwaysRawBankDataEventCreationTriggerStrategy()));

        WireMockConfiguration config = wireMockConfig().dynamicPort().dynamicPort();
        WireMockServer wireMockServer = new WireMockServer(config);
        wireMockServer.start();

        MappingBuilder mappingBuilder1 = WireMock.get(WireMock.urlPathEqualTo("/get_accounts"));
        ResponseDefinitionBuilder response1 = WireMock.aResponse();
        String responseBody1 = "{\"keyA\": \"value1\", \"keyB\": \"value2\"}";
        response1.withBody(responseBody1);
        mappingBuilder1.willReturn(response1);
        wireMockServer.stubFor(mappingBuilder1);

        // when
        String actualResponse1 =
                client.request(
                                String.format(
                                        "http://localhost:%d/get_accounts", wireMockServer.port()))
                        .get(String.class);
        wireMockServer.stop();

        // then
        Assert.assertEquals(responseBody1, actualResponse1);
        Assert.assertEquals(0, eventAccumulator.getEventList().size());
    }

    private void assertExpectedAndActualRawBankDataTrackerEventBankFieldsAreTheSame(
            List<RawBankDataTrackerEventProto.RawBankDataTrackerEventBankField> expected,
            List<RawBankDataTrackerEventProto.RawBankDataTrackerEventBankField> actual) {
        List<String> expectedInString =
                expected.stream().map(AbstractMessage::toString).collect(Collectors.toList());
        List<String> actualInString =
                actual.stream().map(AbstractMessage::toString).collect(Collectors.toList());
        Collections.sort(expectedInString);
        Collections.sort(actualInString);
        Assert.assertEquals(expectedInString, actualInString);
    }

    private String readResource(String filePath) {
        try {
            return new String(
                    Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8.name());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
