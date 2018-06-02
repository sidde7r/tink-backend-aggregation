package se.tink.backend.core.tracking;

import io.protostuff.Tag;
import se.tink.libraries.uuid.UUIDUtils;

public class TrackingSessionResponse {
    @Tag(1)
    private String sessionId;

    public TrackingSessionResponse() {

    }

    public TrackingSessionResponse(TrackingSession session) {
        this.sessionId = UUIDUtils.toTinkUUID(session.getId());
    }
}
