package se.tink.backend.core.tracking;

import io.protostuff.Exclude;
import io.protostuff.Tag;

import java.util.Date;
import java.util.UUID;

import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;

import se.tink.backend.core.Creatable;

@Table(value = "tracking_events")
public class TrackingEvent {
    @Exclude
    @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, ordinal = 1)
    private UUID id;
    @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED, ordinal = 0)
    @Exclude
    private UUID sessionId;
    @Tag(1)
    @Creatable
    private Date date;
    @Tag(2)
    @Creatable
    private String category;
    @Tag(3)
    @Creatable
    private String action;
    @Tag(4)
    @Creatable
    private String label;
    @Tag(5)
    @Creatable
    private Long value;

    public TrackingEvent() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }
}
