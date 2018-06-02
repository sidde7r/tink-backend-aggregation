package se.tink.backend.core;

import com.datastax.driver.core.utils.UUIDs;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.MoreObjects;
import io.protostuff.Exclude;
import io.protostuff.Tag;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;
import se.tink.libraries.uuid.UUIDUtils;

@Table(value = "events")
public class Event {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Tag(3)
    @Creatable
    private String content;
    /**
     * Type 1 UUID which also contains timestamp of the event.
     */
    @Exclude
    @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, ordinal = 1)
    private UUID id;
    @Creatable
    @Tag(2)
    private String type;
    @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED, ordinal = 0)
    @Exclude
    private UUID userId;
    @Tag(1)
    private Date date;

    public Event() {
    }

    public Event(String userId, String type) {
        this(userId, type, (String) null);
    }

    public Event(String userId, String type, Map<String, Object> properties) throws JsonProcessingException {
        this(userId, type, MAPPER.writeValueAsString(properties));
    }

    public Event(String userId, Date date, String type, Map<String, Object> properties) throws JsonProcessingException {
        this(userId, date, type, MAPPER.writeValueAsString(properties));
    }

    public Event(String userId, String type, String content) {
        this(userId, new Date(), type, content);
    }

    public Event(String userId, Date date, String type, String content) {
        this.id = UUIDs.timeBased();
        this.userId = UUIDUtils.fromTinkUUID(userId);
        this.date = date;
        this.type = type;
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public Date getDate() {
        return date;
    }

    /**
     * Returns a type 1 UUID that uniquely identifies this entity.
     * 
     * @return
     */
    public UUID getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        // Primary keys are enough here to later be able to look this up in Cassandra.
        return MoreObjects.toStringHelper(this).add("userId", userId).add("id", id).toString();
    }
}
