package se.tink.backend.core.tracking;

import io.protostuff.Exclude;
import io.protostuff.Tag;
import java.util.Date;
import java.util.UUID;
import se.tink.backend.core.Creatable;

public class TrackingEvent {
    @Exclude
    private UUID id;
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
