package se.tink.backend.aggregation.events;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors.FieldDescriptor;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.internal.util.collections.Sets;
import se.tink.backend.eventproducerservice.grpc.BatchEventAck;
import se.tink.backend.eventproducerservice.grpc.EventAck;
import se.tink.backend.eventproducerservice.grpc.EventAckAsync;
import se.tink.libraries.event_producer_service_client.grpc.EventProducerServiceClient;
import se.tink.libraries.pair.Pair;

public final class DataTrackerEventProducerTest {

    private static class FakeEventProducerServiceClient implements EventProducerServiceClient {

        private Any postedData;

        @Override
        public ListenableFuture<EventAck> postEventAsync(Any data) {
            postedData = data;
            return null;
        }

        @Override
        public ListenableFuture<EventAckAsync> postEventFireAndForget(Any data) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ListenableFuture<BatchEventAck> postEventsBatchAsync(List<Any> data) {
            throw new UnsupportedOperationException();
        }

        Any getPostedData() {
            return Optional.ofNullable(postedData).orElseThrow(IllegalStateException::new);
        }
    }

    @Test
    public void sendDataTrackerEventCallsPostEventAsync() {
        // given
        final FakeEventProducerServiceClient producerClient = new FakeEventProducerServiceClient();

        final DataTrackerEventProducer producer =
                new DataTrackerEventProducer(producerClient, true);

        final String providerName = "hoy";
        final String correlationId = "hoy";
        final String fieldName = "hoy";
        final boolean hasValue = true;
        final String appId = "hoy";
        final String clusterId = "hoy";
        final String userId = "hoy";

        List<Pair<String, Boolean>> data =
                Collections.singletonList(new Pair<>(fieldName, hasValue));

        // when
        producer.sendDataTrackerEvent(providerName, correlationId, data, appId, clusterId, userId);

        final Map<FieldDescriptor, Object> fields = producerClient.getPostedData().getAllFields();
        final Set<String> keys =
                fields.keySet().stream().map(FieldDescriptor::getName).collect(Collectors.toSet());
        final Collection<Object> values = fields.values();

        // then
        Assert.assertEquals(2, fields.size());

        final String typeUrlValue =
                (String)
                        fields.entrySet().stream()
                                .filter(e -> Objects.equals(e.getKey().getName(), "type_url"))
                                .findFirst()
                                .get()
                                .getValue();
        final byte[] valueValue =
                ((ByteString)
                                fields.entrySet().stream()
                                        .filter(e -> Objects.equals(e.getKey().getName(), "value"))
                                        .findFirst()
                                        .get()
                                        .getValue())
                        .toByteArray();

        assertThat(keys, is(Sets.newSet("type_url", "value")));
        Assert.assertEquals("type.googleapis.com/proto.DataTrackerEvent", typeUrlValue);
        Assert.assertTrue(new String(valueValue).contains("hoy"));
    }
}
