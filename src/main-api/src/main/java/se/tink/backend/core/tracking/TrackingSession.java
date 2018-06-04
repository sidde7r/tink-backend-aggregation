package se.tink.backend.core.tracking;

import java.util.UUID;

public class TrackingSession {
    private UUID id;
    private UUID userId;

    public TrackingSession() {

    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }
}
