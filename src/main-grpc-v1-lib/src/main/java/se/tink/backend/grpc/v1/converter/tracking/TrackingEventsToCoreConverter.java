package se.tink.backend.grpc.v1.converter.tracking;

import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.utils.ProtobufModelUtils;
import se.tink.grpc.v1.models.TrackingEvent;

public class TrackingEventsToCoreConverter implements
        Converter<TrackingEvent, se.tink.backend.core.tracking.TrackingEvent> {
    @Override
    public se.tink.backend.core.tracking.TrackingEvent convertFrom(TrackingEvent input) {
        se.tink.backend.core.tracking.TrackingEvent event = new se.tink.backend.core.tracking.TrackingEvent();
        event.setDate(input.hasDate() ? ProtobufModelUtils.timestampToDate(input.getDate()) : null);
        event.setCategory(input.getCategory());
        event.setAction(input.getAction());
        event.setLabel(input.getLabel());
        event.setValue(input.getDefaultValue());
        return event;
    }
}
