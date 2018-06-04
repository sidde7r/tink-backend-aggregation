package se.tink.backend.core.tracking;

import io.protostuff.Exclude;
import io.protostuff.Tag;
import java.util.Date;
import java.util.UUID;

public class TrackingView {
    @Exclude
    private UUID id;
    @Exclude
    private UUID sessionId;
    @Tag(1)
    private Date date;
    @Tag(2)
    private String name;

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
