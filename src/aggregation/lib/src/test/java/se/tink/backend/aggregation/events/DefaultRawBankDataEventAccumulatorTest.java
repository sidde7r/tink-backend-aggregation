package se.tink.backend.aggregation.events;

import com.google.protobuf.AbstractMessage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import se.tink.eventproducerservice.events.grpc.RawBankDataTrackerEventProto;
import se.tink.eventproducerservice.events.grpc.RawBankDataTrackerEventProto.RawBankDataTrackerEventBankField;
import se.tink.eventproducerservice.events.grpc.RawBankDataTrackerEventProto.RawBankDataTrackerEventBankFieldType;

public class DefaultRawBankDataEventAccumulatorTest {

    @Test
    public void
            whenGivenAProperComplexJsonResponseBodyEventProducerShouldEmitEventAndAggregatorShouldAggregate() {

        // given
        String responseBodyFilePath =
                "src/aggregation/lib/src/test/java/se/tink/backend/aggregation/events/resources/aggregator_test.json";
        String givenResponseBody = readResource(responseBodyFilePath);
        se.tink.backend.aggregation.nxgen.http.event.event_producers.DefaultRawBankDataEventProducer
                defaultRawBankDataEventProducer =
                        new se.tink.backend.aggregation.nxgen.http.event.event_producers
                                .DefaultRawBankDataEventProducer(
                                se.tink.backend.aggregation.nxgen.http.event.configuration
                                        .RawBankDataEventCreationStrategies
                                        .createDefaultConfiguration());
        se.tink.backend.aggregation.nxgen.http.event.event_producers.RawBankDataEventAccumulator
                accumulator =
                        new se.tink.backend.aggregation.nxgen.http.event.event_producers
                                .RawBankDataEventAccumulator();

        List<RawBankDataTrackerEventProto.RawBankDataTrackerEventBankField> expectedFields =
                new ArrayList<>();
        expectedFields.add(
                RawBankDataTrackerEventBankField.newBuilder()
                        .setFieldPath("transactions.booked[].transactionId")
                        .setFieldType(RawBankDataTrackerEventBankFieldType.UNKNOWN)
                        .setIsFieldSet(true)
                        .setIsFieldMasked(true)
                        .setFieldValue("MASKED")
                        .setCount(2)
                        .build());
        expectedFields.add(
                RawBankDataTrackerEventBankField.newBuilder()
                        .setFieldPath("transactions.booked[].valueDate")
                        .setFieldType(RawBankDataTrackerEventBankFieldType.DATE)
                        .setIsFieldSet(true)
                        .setIsFieldMasked(false)
                        .setFieldValue("2021-05-18")
                        .setCount(2)
                        .build());

        // when
        accumulator.addEvent(
                defaultRawBankDataEventProducer
                        .produceRawBankDataEvent(givenResponseBody, "dummy-correlationId")
                        .orElseThrow(() -> new IllegalStateException("No events produced")));

        // then
        Assert.assertEquals(1, accumulator.getEventList().size());

        List<RawBankDataTrackerEventProto.RawBankDataTrackerEventBankField> actualFields =
                accumulator.getEventList().get(0).getFieldDataList();
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
