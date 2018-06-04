package se.tink.backend.core.tracking;

import io.protostuff.Exclude;
import io.protostuff.Tag;
import java.util.Date;
import java.util.UUID;

public class TrackingTiming {
    @Exclude
    private UUID id;
    @Exclude
    private UUID sessionId;
    @Tag(1)
    private Date date;
    @Tag(2)
    private String category;
    @Tag(3)
    private Long time;
    @Tag(4)
    private String label;
    @Tag(5)
    private String name;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
}
