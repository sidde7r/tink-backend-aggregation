package se.tink.backend.common.tasks.kafka;

import com.google.api.client.util.Maps;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

public class KafkaCommit {

    public TopicPartition partition;
    public long offset;
    public int delta;

    public KafkaCommit(TopicPartition partition, long offset, int delta) {

        this.partition = partition;
        this.offset = offset;
        this.delta = delta;
    }

    public Map<TopicPartition, OffsetAndMetadata> toMap() {
        HashMap<TopicPartition, OffsetAndMetadata> map = Maps.newHashMap();
        map.put(partition, new OffsetAndMetadata(offset));
        return map;
    }
}
