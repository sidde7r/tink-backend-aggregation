package se.tink.backend.main.transports;

import com.google.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;
import se.tink.backend.api.TrackingService;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.core.tracking.TrackingRequest;
import se.tink.backend.core.tracking.TrackingSessionResponse;
import se.tink.backend.main.controllers.TrackingServiceController;
import se.tink.backend.main.transports.converters.tracking.TrackingSessionCommandConverter;

public class TrackingServiceJerseyTransport implements TrackingService {

    @Context
    private HttpHeaders headers;
    @Context
    private HttpServletRequest request;
    @Context
    private UriInfo uriInfo;

    private TrackingServiceController trackingServiceController;

    @Inject
    public TrackingServiceJerseyTransport(
            TrackingServiceController trackingServiceController) {
        this.trackingServiceController = trackingServiceController;
    }

    @Override
    public TrackingSessionResponse createSession() {
        return new TrackingSessionResponse(trackingServiceController.createSession());
    }

    @Override
    public void trackData(AuthenticatedUser authenticatedUser, String sessionId, TrackingRequest trackingRequest) {
        trackingServiceController
                .track(TrackingSessionCommandConverter.convertFrom(authenticatedUser, sessionId, headers, trackingRequest));
    }
}
