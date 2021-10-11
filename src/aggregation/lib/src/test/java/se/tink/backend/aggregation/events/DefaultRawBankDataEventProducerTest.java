package se.tink.backend.aggregation.events;

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
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.http.event.configuration.RawBankDataEventEmissionConfiguration;
import se.tink.backend.aggregation.nxgen.http.event.decision_strategy.DenyAlwaysRawBankDataEventEmissionDecisionStrategy;
import se.tink.backend.aggregation.nxgen.http.event.event_producers.DefaultRawBankDataEventProducer;
import se.tink.eventproducerservice.events.grpc.RawBankDataTrackerEventProto;
import se.tink.eventproducerservice.events.grpc.RawBankDataTrackerEventProto.RawBankDataTrackerEvent;
import se.tink.eventproducerservice.events.grpc.RawBankDataTrackerEventProto.RawBankDataTrackerEventBankField;
import se.tink.eventproducerservice.events.grpc.RawBankDataTrackerEventProto.RawBankDataTrackerEventBankFieldType;

@Slf4j
public class DefaultRawBankDataEventProducerTest {

    @Test
    public void whenGivenAProperJsonResponseBodyEventProducerShouldEmitEvent() {

        // given
        DefaultRawBankDataEventProducer defaultRawBankDataEventProducer =
                new DefaultRawBankDataEventProducer();
        String givenResponseBody = "{\"key\": \"value\"}";

        // when
        Optional<RawBankDataTrackerEvent> event =
                defaultRawBankDataEventProducer.produceRawBankDataEvent(
                        RawBankDataEventEmissionConfiguration.allowEmissionWithDefaultSettings(),
                        givenResponseBody,
                        "dummy-correlationId");

        // then
        Assert.assertTrue(event.isPresent());
    }

    @Test
    public void
            whenGivenAProperJsonResponseBodyEventProducerShouldNotEmitEventIfConfigurationDeniesEmission() {

        // given
        DefaultRawBankDataEventProducer defaultRawBankDataEventProducer =
                new DefaultRawBankDataEventProducer();
        String givenResponseBody = "{\"key\": \"value\"}";

        // when
        Optional<RawBankDataTrackerEvent> event =
                defaultRawBankDataEventProducer.produceRawBankDataEvent(
                        RawBankDataEventEmissionConfiguration.builder()
                                .emissionDecisionStrategy(
                                        new DenyAlwaysRawBankDataEventEmissionDecisionStrategy())
                                .build(),
                        givenResponseBody,
                        "dummy-correlationId");

        // then
        Assert.assertFalse(event.isPresent());
    }

    @Test
    public void whenGivenANonJsonResponseBodyEventProducerShouldNotEmitEvent() {

        // given
        DefaultRawBankDataEventProducer defaultRawBankDataEventProducer =
                new DefaultRawBankDataEventProducer();
        String givenResponseBody = "non json data";

        // when
        Optional<RawBankDataTrackerEvent> event =
                defaultRawBankDataEventProducer.produceRawBankDataEvent(
                        RawBankDataEventEmissionConfiguration.allowEmissionWithDefaultSettings(),
                        givenResponseBody,
                        "dummy-correlationId");

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
                new DefaultRawBankDataEventProducer();

        List<RawBankDataTrackerEventProto.RawBankDataTrackerEventBankField> expectedFields =
                new ArrayList<>();
        expectedFields.add(
                RawBankDataTrackerEventBankField.newBuilder()
                        .setFieldPath("dummy_value")
                        .setFieldType(RawBankDataTrackerEventBankFieldType.UNKNOWN)
                        .setIsFieldSet(true)
                        .setIsFieldMasked(true)
                        .setFieldValue("MASKED")
                        .build());
        expectedFields.add(
                RawBankDataTrackerEventBankField.newBuilder()
                        .setFieldPath("weird.field")
                        .setFieldType(RawBankDataTrackerEventBankFieldType.UNKNOWN)
                        .setIsFieldSet(true)
                        .setIsFieldMasked(true)
                        .setFieldValue("MASKED")
                        .build());
        expectedFields.add(
                RawBankDataTrackerEventBankField.newBuilder()
                        .setFieldPath("dummy_null_value")
                        .setFieldType(RawBankDataTrackerEventBankFieldType.UNKNOWN)
                        .setIsFieldSet(false)
                        .setIsFieldMasked(true)
                        .setFieldValue("MASKED")
                        .build());
        expectedFields.add(
                RawBankDataTrackerEventBankField.newBuilder()
                        .setFieldPath("iban")
                        .setFieldType(RawBankDataTrackerEventBankFieldType.UNKNOWN)
                        .setIsFieldSet(true)
                        .setIsFieldMasked(true)
                        .setFieldValue("MASKED")
                        .build());
        expectedFields.add(
                RawBankDataTrackerEventBankField.newBuilder()
                        .setFieldPath("bban")
                        .setFieldType(RawBankDataTrackerEventBankFieldType.UNKNOWN)
                        .setIsFieldSet(true)
                        .setIsFieldMasked(true)
                        .setFieldValue("MASKED")
                        .build());
        expectedFields.add(
                RawBankDataTrackerEventBankField.newBuilder()
                        .setFieldPath("transactions.booked[].transactionId")
                        .setFieldType(RawBankDataTrackerEventBankFieldType.UNKNOWN)
                        .setIsFieldSet(true)
                        .setIsFieldMasked(true)
                        .setFieldValue("MASKED")
                        .build());
        expectedFields.add(
                RawBankDataTrackerEventBankField.newBuilder()
                        .setFieldPath("transactions.booked[].valueDate")
                        .setFieldType(RawBankDataTrackerEventBankFieldType.UNKNOWN)
                        .setIsFieldSet(true)
                        .setIsFieldMasked(true)
                        .setFieldValue("MASKED")
                        .build());
        expectedFields.add(
                RawBankDataTrackerEventBankField.newBuilder()
                        .setFieldPath("transactions.booked[].bookingDate")
                        .setFieldType(RawBankDataTrackerEventBankFieldType.UNKNOWN)
                        .setIsFieldSet(true)
                        .setIsFieldMasked(true)
                        .setFieldValue("MASKED")
                        .build());
        expectedFields.add(
                RawBankDataTrackerEventBankField.newBuilder()
                        .setFieldPath("transactions.booked[].weird.field")
                        .setFieldType(RawBankDataTrackerEventBankFieldType.UNKNOWN)
                        .setIsFieldSet(true)
                        .setIsFieldMasked(true)
                        .setFieldValue("MASKED")
                        .build());
        expectedFields.add(
                RawBankDataTrackerEventBankField.newBuilder()
                        .setFieldPath("transactions.booked[].weird[]Field")
                        .setFieldType(RawBankDataTrackerEventBankFieldType.UNKNOWN)
                        .setIsFieldSet(true)
                        .setIsFieldMasked(true)
                        .setFieldValue("MASKED")
                        .build());
        expectedFields.add(
                RawBankDataTrackerEventBankField.newBuilder()
                        .setFieldPath("transactions.booked[].transactionAmount.amount")
                        .setFieldType(RawBankDataTrackerEventBankFieldType.UNKNOWN)
                        .setIsFieldSet(true)
                        .setIsFieldMasked(true)
                        .setFieldValue("MASKED")
                        .build());
        expectedFields.add(
                RawBankDataTrackerEventBankField.newBuilder()
                        .setFieldPath("transactions.booked[].transactionAmount.currency")
                        .setFieldType(RawBankDataTrackerEventBankFieldType.UNKNOWN)
                        .setIsFieldSet(true)
                        .setIsFieldMasked(true)
                        .setFieldValue("MASKED")
                        .build());
        expectedFields.add(
                RawBankDataTrackerEventBankField.newBuilder()
                        .setFieldPath("transactions.booked[]._links.transactionDetails.href")
                        .setFieldType(RawBankDataTrackerEventBankFieldType.UNKNOWN)
                        .setIsFieldSet(true)
                        .setIsFieldMasked(true)
                        .setFieldValue("MASKED")
                        .build());
        expectedFields.add(
                RawBankDataTrackerEventBankField.newBuilder()
                        .setFieldPath("transactions.booked[].transactionId")
                        .setFieldType(RawBankDataTrackerEventBankFieldType.UNKNOWN)
                        .setIsFieldSet(true)
                        .setIsFieldMasked(true)
                        .setFieldValue("MASKED")
                        .build());
        expectedFields.add(
                RawBankDataTrackerEventBankField.newBuilder()
                        .setFieldPath("transactions.booked[].valueDate")
                        .setFieldType(RawBankDataTrackerEventBankFieldType.UNKNOWN)
                        .setIsFieldSet(true)
                        .setIsFieldMasked(true)
                        .setFieldValue("MASKED")
                        .build());
        expectedFields.add(
                RawBankDataTrackerEventBankField.newBuilder()
                        .setFieldPath("transactions.booked[].bookingDate")
                        .setFieldType(RawBankDataTrackerEventBankFieldType.UNKNOWN)
                        .setIsFieldSet(true)
                        .setIsFieldMasked(true)
                        .setFieldValue("MASKED")
                        .build());
        expectedFields.add(
                RawBankDataTrackerEventBankField.newBuilder()
                        .setFieldPath("transactions.booked[].transactionAmount.amount")
                        .setFieldType(RawBankDataTrackerEventBankFieldType.UNKNOWN)
                        .setIsFieldSet(true)
                        .setIsFieldMasked(true)
                        .setFieldValue("MASKED")
                        .build());
        expectedFields.add(
                RawBankDataTrackerEventBankField.newBuilder()
                        .setFieldPath("transactions.booked[].transactionAmount.currency")
                        .setFieldType(RawBankDataTrackerEventBankFieldType.UNKNOWN)
                        .setIsFieldSet(true)
                        .setIsFieldMasked(true)
                        .setFieldValue("MASKED")
                        .build());
        expectedFields.add(
                RawBankDataTrackerEventBankField.newBuilder()
                        .setFieldPath("transactions.booked[]._links.transactionDetails.href")
                        .setFieldType(RawBankDataTrackerEventBankFieldType.UNKNOWN)
                        .setIsFieldSet(true)
                        .setIsFieldMasked(true)
                        .setFieldValue("MASKED")
                        .build());
        expectedFields.add(
                RawBankDataTrackerEventBankField.newBuilder()
                        .setFieldPath("transactions.pending[].transactionAmount.amount")
                        .setFieldType(RawBankDataTrackerEventBankFieldType.UNKNOWN)
                        .setIsFieldSet(true)
                        .setIsFieldMasked(true)
                        .setFieldValue("MASKED")
                        .build());
        expectedFields.add(
                RawBankDataTrackerEventBankField.newBuilder()
                        .setFieldPath("transactions.pending[].transactionAmount.currency")
                        .setFieldType(RawBankDataTrackerEventBankFieldType.UNKNOWN)
                        .setIsFieldSet(true)
                        .setIsFieldMasked(true)
                        .setFieldValue("MASKED")
                        .build());
        expectedFields.add(
                RawBankDataTrackerEventBankField.newBuilder()
                        .setFieldPath("transactions.pending[].valueDate")
                        .setFieldType(RawBankDataTrackerEventBankFieldType.UNKNOWN)
                        .setIsFieldSet(true)
                        .setIsFieldMasked(true)
                        .setFieldValue("MASKED")
                        .build());
        expectedFields.add(
                RawBankDataTrackerEventBankField.newBuilder()
                        .setFieldPath("transactions.pending[].descriptiveText")
                        .setFieldType(RawBankDataTrackerEventBankFieldType.UNKNOWN)
                        .setIsFieldSet(true)
                        .setIsFieldMasked(true)
                        .setFieldValue("MASKED")
                        .build());
        expectedFields.add(
                RawBankDataTrackerEventBankField.newBuilder()
                        .setFieldPath("transactions.pending[].pendingType")
                        .setFieldType(RawBankDataTrackerEventBankFieldType.UNKNOWN)
                        .setIsFieldSet(true)
                        .setIsFieldMasked(true)
                        .setFieldValue("MASKED")
                        .build());
        expectedFields.add(
                RawBankDataTrackerEventBankField.newBuilder()
                        .setFieldPath("_links.account.href")
                        .setFieldType(RawBankDataTrackerEventBankFieldType.UNKNOWN)
                        .setIsFieldSet(true)
                        .setIsFieldMasked(true)
                        .setFieldValue("MASKED")
                        .build());

        // when
        Optional<RawBankDataTrackerEvent> event =
                defaultRawBankDataEventProducer.produceRawBankDataEvent(
                        RawBankDataEventEmissionConfiguration.allowEmissionWithDefaultSettings(),
                        givenResponseBody,
                        "dummy-correlationId");

        // then
        Assert.assertTrue(event.isPresent());

        List<RawBankDataTrackerEventProto.RawBankDataTrackerEventBankField> actualFields =
                event.get().getFieldDataList();
        assertExpectedAndActualRawBankDataTrackerEventBankFieldsAreTheSame(
                expectedFields, actualFields);
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
