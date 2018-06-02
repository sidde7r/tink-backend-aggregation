package se.tink.backend.grpc.v1.converter.tracking;

import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.utils.ProtobufModelUtils;
import se.tink.grpc.v1.models.TrackingView;

public class TrackingViewsToCoreConverter implements
        Converter<TrackingView, se.tink.backend.core.tracking.TrackingView> {
    @Override
    public se.tink.backend.core.tracking.TrackingView convertFrom(TrackingView input) {
        se.tink.backend.core.tracking.TrackingView event = new se.tink.backend.core.tracking.TrackingView();
        event.setDate(input.hasDate() ? ProtobufModelUtils.timestampToDate(input.getDate()) : null);
        event.setName(input.getName());
        return event;
    }
}
