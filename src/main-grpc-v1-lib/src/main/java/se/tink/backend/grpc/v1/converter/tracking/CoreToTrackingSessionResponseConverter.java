package se.tink.backend.grpc.v1.converter.tracking;

import se.tink.backend.core.tracking.TrackingSession;
import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.grpc.v1.rpc.TrackingSessionResponse;
import se.tink.libraries.uuid.UUIDUtils;

public class CoreToTrackingSessionResponseConverter implements
        Converter<TrackingSession, TrackingSessionResponse> {

    @Override
    public TrackingSessionResponse convertFrom(TrackingSession input) {
        return TrackingSessionResponse.newBuilder().setSessionId(UUIDUtils.toTinkUUID(input.getId())).build();
    }
}
