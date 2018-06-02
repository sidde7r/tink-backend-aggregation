package se.tink.backend.system.tasks;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "taskType")
@JsonSubTypes({
        @Type(value = UpdateTransactionsTask.class, name = "UpdateTransactionsTask"),
        @Type(value = CheckpointRollbackTask.class, name = "CheckpointRollbackTask"),
        @Type(value = DeleteTransactionTask.class, name = "DeleteTransactionTask"),
})
public abstract class Task<P> {

    public static final String UPDATE_TRANSACTIONS_TOPIC = "UPDATE_TRANSACTIONS";
    public static final String UPDATE_HIGH_PRIO_TRANSACTIONS_TOPIC = "UPDATE_HIGH_PRIO_TRANSACTIONS";
    public static final ImmutableList<String> TOPICS = ImmutableList.of(UPDATE_TRANSACTIONS_TOPIC,
            UPDATE_HIGH_PRIO_TRANSACTIONS_TOPIC);
    
    private P payload;
    private final String topic;
    private Object partitionKey;
    
    protected Task(String topic) {
        this.topic = topic;
    }

    public P getPayload() {
        return payload;
    }
    
    public String getTopic() {
        return topic;
    }
    
    public void setPayload(P payload) {
        this.payload = payload;
    }

    public Object getPartitionKey() {
        return partitionKey;
    }

    public void setPartitionKey(Object partitionKey) {
        this.partitionKey = partitionKey;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("topic", topic).add("payload", payload).toString();
    }
}
