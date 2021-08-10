package se.tink.backend.aggregation.events;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.Test;
import org.mockito.internal.util.collections.Sets;
import se.tink.eventproducerservice.events.grpc.DataTrackerEventProto;
import se.tink.libraries.events.api.SubmitEventException;

public final class DataTrackerEventProducerTest {

    @Test
    public void sendDataTrackerEventCallsPostEventAsync() throws SubmitEventException {
        // given
        final DataTrackerEventProducer producer = new DataTrackerEventProducer(true);

        final String providerName = "hoy";
        final String correlationId = "hoy";
        final String fieldName = "hoy";
        final String fieldValue = "hoy";
        final boolean hasValue = true;
        final String appId = "hoy";
        final String clusterId = "hoy";
        final String userId = "hoy";

        Map<String, Boolean> isFieldPopulated = new HashMap<>();
        Map<String, String> fieldValues = new HashMap<>();

        isFieldPopulated.put(fieldName, hasValue);
        fieldValues.put(fieldName, fieldValue);

        // when
        DataTrackerEventProto.DataTrackerEvent event =
                producer.produceDataTrackerEvent(
                        providerName,
                        correlationId,
                        isFieldPopulated,
                        fieldValues,
                        appId,
                        clusterId,
                        userId);

        List<Message> messages = producer.toMessages(Collections.singletonList(event));

        assertEquals(1, messages.size());
        Message message = messages.get(0);

        final Map<FieldDescriptor, Object> fields = message.getAllFields();
        final Set<String> keys =
                fields.keySet().stream().map(FieldDescriptor::getName).collect(Collectors.toSet());

        // then
        assertEquals(7, fields.size());
        final String providerNameValue =
                (String)
                        fields.entrySet().stream()
                                .filter(e -> Objects.equals(e.getKey().getName(), "provider_name"))
                                .findFirst()
                                .get()
                                .getValue();

        assertThat(
                keys,
                is(
                        Sets.newSet(
                                "cluster_id",
                                "user_id",
                                "field_data",
                                "correlation_id",
                                "provider_name",
                                "app_id",
                                "timestamp")));

        assertEquals("hoy", providerNameValue);
    }
}
