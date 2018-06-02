package se.tink.backend.grpc.v1.converter.tracking;

import java.util.Date;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.auth.AuthenticationContext;
import se.tink.backend.core.User;
import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.rpc.TrackSessionCommand;
import se.tink.api.headers.TinkHttpHeaders;
import se.tink.grpc.v1.rpc.TrackingRequest;
import se.tink.libraries.date.DateUtils;

public class TrackingRequestToCommandConverter implements Converter<TrackingRequest, TrackSessionCommand> {

    private User user;
    private AuthenticationContext context;

    public TrackingRequestToCommandConverter(User user, AuthenticationContext context) {
        this.user = user;
        this.context = context;
    }

    @Override
    public TrackSessionCommand convertFrom(TrackingRequest input) {

        Optional<String> userId = Optional.ofNullable(user).map(User::getId);

        return new TrackSessionCommand(input.getSessionId(), userId,
                new TrackingEventsToCoreConverter().convertFrom(input.getEventsList()),
                new TrackingTimingsToCoreConverter().convertFrom(input.getTimingsList()),
                new TrackingViewsToCoreConverter().convertFrom(input.getViewsList()),
                getClientClock(context.getMetadata()));
    }

    private Date getClientClock(Map<String, String> headers) {
        if (!headers.containsKey(TinkHttpHeaders.CLIENT_CLOCK_HEADER_NAME)) {
            return null;
        }
        return DateUtils.parseDate(headers.get(TinkHttpHeaders.CLIENT_CLOCK_HEADER_NAME));
    }
}
