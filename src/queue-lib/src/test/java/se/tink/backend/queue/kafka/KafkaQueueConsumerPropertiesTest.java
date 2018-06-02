package se.tink.backend.queue.kafka;

import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class KafkaQueueConsumerPropertiesTest {

    @Test
    public void testDefaultProperties() {
        KafkaQueueConsumerProperties properties = new KafkaQueueConsumerProperties();

        properties.setHosts("127.0.0.1");
        properties.setStartFromLastMessage(true);
        properties.setGroupId("group-id");

        assertThat(properties.getGroupId()).isEqualTo("group-id");
        assertThat(properties.shouldStartFromLastMessage()).isTrue();
        assertThat(properties.getHosts()).isEqualTo("127.0.0.1");
    }

    @Test
    public void testDefaultMaxPollRecords() {
        KafkaQueueConsumerProperties properties = new KafkaQueueConsumerProperties();
        assertThat(properties.getMaxPollRecords().isPresent()).isFalse();
    }

    @Test
    public void testMaxPollRecordsWithValue() {
        KafkaQueueConsumerProperties properties = new KafkaQueueConsumerProperties();
        properties.setMaxPollRecords(100);
        assertThat(properties.getMaxPollRecords().isPresent()).isTrue();
        assertThat(properties.getMaxPollRecords().get()).isEqualTo(100);
    }
}
