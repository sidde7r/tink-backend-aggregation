package se.tink.backend.aggregation.workers.commands;

import com.google.protobuf.Message;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import se.tink.eventproducerservice.events.grpc.RawBankDataTrackerEventProto.RawBankDataTrackerEvent;

public class EventListSplitterTest {

    @Test
    public void eventListSplitterShouldSplitTheListCorrectly() {
        // given

        // This EventListSplitter allows a list to have at most 100 bytes
        final EventListSplitter sut = new EventListSplitter(100);

        // This message is 62 bytes
        final Message message1 =
                RawBankDataTrackerEvent.newBuilder()
                        .setCorrelationId("dummy-correlation-id1")
                        .setEventId("dummy-event-id1")
                        .setProviderName("dummy-provider-name1")
                        .build();

        // This message is also 62 bytes
        final Message message2 =
                RawBankDataTrackerEvent.newBuilder()
                        .setCorrelationId("dummy-correlation-id2")
                        .setEventId("dummy-event-id2")
                        .setProviderName("dummy-provider-name2")
                        .build();

        final List<Message> messageList = Arrays.asList(message1, message2);

        // when
        List<List<Message>> messageListChunks = sut.splitMessageListIntoChunks(messageList);

        // then
        Assert.assertEquals(2, messageListChunks.size());
        Assert.assertEquals(1, messageListChunks.get(0).size());
        Assert.assertEquals(1, messageListChunks.get(1).size());
        Assert.assertEquals(message1, messageListChunks.get(0).get(0));
        Assert.assertEquals(message2, messageListChunks.get(1).get(0));
    }

    @Test
    public void eventListSplitterShouldNotSplitTheListIfSizeIsSmallerThanAllowed() {
        // given

        // This EventListSplitter allows a list to have at most 150 bytes
        final EventListSplitter sut = new EventListSplitter(150);

        // This message is 62 bytes
        final Message message1 =
                RawBankDataTrackerEvent.newBuilder()
                        .setCorrelationId("dummy-correlation-id1")
                        .setEventId("dummy-event-id1")
                        .setProviderName("dummy-provider-name1")
                        .build();

        // This message is also 62 bytes
        final Message message2 =
                RawBankDataTrackerEvent.newBuilder()
                        .setCorrelationId("dummy-correlation-id2")
                        .setEventId("dummy-event-id2")
                        .setProviderName("dummy-provider-name2")
                        .build();

        final List<Message> messageList = Arrays.asList(message1, message2);

        // when
        List<List<Message>> messageListChunks = sut.splitMessageListIntoChunks(messageList);

        // then
        Assert.assertEquals(1, messageListChunks.size());
        Assert.assertEquals(2, messageListChunks.get(0).size());
        Assert.assertEquals(message1, messageListChunks.get(0).get(0));
        Assert.assertEquals(message2, messageListChunks.get(0).get(1));
    }

    @Test
    public void eventListSplitterShouldIgnoreTooBigEvents() {
        // given

        // This EventListSplitter allows a list to have at most 100 bytes
        final EventListSplitter sut = new EventListSplitter(100);

        // This message is 62 bytes
        final Message message1 =
                RawBankDataTrackerEvent.newBuilder()
                        .setCorrelationId("dummy-correlation-id1")
                        .setEventId("dummy-event-id1")
                        .setProviderName("dummy-provider-name1")
                        .build();

        // This message is also 62 bytes
        final Message message2 =
                RawBankDataTrackerEvent.newBuilder()
                        .setCorrelationId("dummy-correlation-id2")
                        .setEventId("dummy-event-id2")
                        .setProviderName("dummy-provider-name2")
                        .build();

        // This message is bigger than 100 bytes (bigger than allowed size)
        final Message bigMessage =
                RawBankDataTrackerEvent.newBuilder()
                        .setCorrelationId(new String(new byte[101]))
                        .build();

        final List<Message> messageList = Arrays.asList(message1, message2, bigMessage);

        // when
        List<List<Message>> messageListChunks = sut.splitMessageListIntoChunks(messageList);

        // then
        Assert.assertEquals(2, messageListChunks.size());
        Assert.assertEquals(1, messageListChunks.get(0).size());
        Assert.assertEquals(1, messageListChunks.get(1).size());
        Assert.assertEquals(message1, messageListChunks.get(0).get(0));
        Assert.assertEquals(message2, messageListChunks.get(1).get(0));
    }
}
