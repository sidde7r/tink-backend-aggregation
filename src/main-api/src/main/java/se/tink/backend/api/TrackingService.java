package se.tink.backend.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import se.tink.api.annotations.Team;
import se.tink.api.annotations.TeamOwnership;
import se.tink.backend.auth.Authenticated;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.core.tracking.TrackingRequest;
import se.tink.backend.core.tracking.TrackingSessionResponse;
import se.tink.backend.rpc.TinkMediaType;

@Path("/api/v1/tracking")
public interface TrackingService {
    @POST
    @Produces({
            MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF
    })
    @TeamOwnership(Team.PFM)
    TrackingSessionResponse createSession();

    @POST
    @Path("{sessionId}")
    @TeamOwnership(Team.PFM)
    @Consumes({
            MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF
    })
    void trackData(@Authenticated(required = false) AuthenticatedUser authenticatedUser,
            @PathParam("sessionId") String sessionId, TrackingRequest request);

}
