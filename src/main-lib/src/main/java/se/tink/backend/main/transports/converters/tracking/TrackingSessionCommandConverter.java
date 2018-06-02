package se.tink.backend.main.transports.converters.tracking;

import com.google.common.collect.Lists;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.core.HttpHeaders;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.common.resources.RequestHeaderUtils;
import se.tink.backend.core.tracking.TrackingEvent;
import se.tink.backend.core.tracking.TrackingRequest;
import se.tink.backend.core.tracking.TrackingTiming;
import se.tink.backend.core.tracking.TrackingView;
import se.tink.backend.rpc.TrackSessionCommand;

public class TrackingSessionCommandConverter {
    public static TrackSessionCommand convertFrom(AuthenticatedUser user, String sessionId, HttpHeaders httpHeaders, TrackingRequest request) {
        Optional<String> userId = Optional.empty();
        if (user != null) {
            userId = Optional.of(user.getUser().getId());
        }
        List<TrackingEvent> events = request.getEvents() != null ? request.getEvents() : Lists.newArrayList();
        List<TrackingTiming> timings = request.getTimings() != null ? request.getTimings() : Lists.newArrayList();
        List<TrackingView> views = request.getViews() != null ? request.getViews() : Lists.newArrayList();
        Date clientClock = RequestHeaderUtils.getClientClock(httpHeaders);
        return new TrackSessionCommand(sessionId, userId, events, timings, views, clientClock);
    }
}
