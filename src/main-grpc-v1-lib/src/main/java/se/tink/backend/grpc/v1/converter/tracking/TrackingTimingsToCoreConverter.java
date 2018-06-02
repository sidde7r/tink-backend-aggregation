package se.tink.backend.grpc.v1.converter.tracking;

import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.utils.ProtobufModelUtils;
import se.tink.grpc.v1.models.TrackingTiming;

public class TrackingTimingsToCoreConverter implements
        Converter<TrackingTiming, se.tink.backend.core.tracking.TrackingTiming> {
    @Override
    public se.tink.backend.core.tracking.TrackingTiming convertFrom(TrackingTiming input) {
        se.tink.backend.core.tracking.TrackingTiming event = new se.tink.backend.core.tracking.TrackingTiming();
        Long time = null;
        if (input.hasTime()) {
            time = ProtobufModelUtils.timestampToDate(input.getTime()).getTime();
        }
        event.setDate(input.hasDate() ? ProtobufModelUtils.timestampToDate(input.getDate()) : null);
        event.setCategory(input.getCategory());
        event.setTime(time);
        event.setLabel(input.getLabel());
        event.setName(input.getName());
        return event;
    }
}
