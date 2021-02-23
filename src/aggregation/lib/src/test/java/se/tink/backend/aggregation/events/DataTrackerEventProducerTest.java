package se.tink.backend.aggregation.events;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.internal.util.collections.Sets;
import se.tink.eventproducerservice.events.grpc.DataTrackerEventProto;
import se.tink.libraries.events.api.EventSubmitter;
import se.tink.libraries.events.api.SubmitEventException;
import se.tink.libraries.events.guice.EventSubmitterProvider;
import se.tink.libraries.pair.Pair;

public final class DataTrackerEventProducerTest {

    private static class FakeEventSubmitter implements EventSubmitter {

        private List<Message> postedData;

        public FakeEventSubmitter() {
            postedData = new ArrayList<>();
        }

        @Override
        public void submit(Message message) throws SubmitEventException {
            postedData.add(message);
        }

        @Override
        public void submit(List<Message> message) throws SubmitEventException {
            postedData.addAll(message);
        }

        public List<Message> getMessages() {
            return postedData;
        }
    }

    @Test
    public void sendDataTrackerEventCallsPostEventAsync() throws SubmitEventException {
        // given
        EventSubmitter eventSubmitter = new FakeEventSubmitter();
        EventSubmitterProvider eventSubmitterProvider = mock(EventSubmitterProvider.class);
        when(eventSubmitterProvider.get()).thenReturn(eventSubmitter);

        final DataTrackerEventProducer producer =
                new DataTrackerEventProducer(eventSubmitterProvider, true);

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
        DataTrackerEventProto.DataTrackerEvent event =
                producer.produceDataTrackerEvent(
                        providerName, correlationId, data, appId, clusterId, userId);
        eventSubmitterProvider.get().submit(event);

        final Map<FieldDescriptor, Object> fields = event.getAllFields();
        final Set<String> keys =
                fields.keySet().stream().map(FieldDescriptor::getName).collect(Collectors.toSet());

        // then
        Assert.assertEquals(7, fields.size());
        Assert.assertEquals(1, ((FakeEventSubmitter) eventSubmitter).getMessages().size());

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

        Assert.assertEquals("hoy", providerNameValue);
    }
}
