package se.tink.backend.aggregationcontroller.v1.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import se.tink.api.annotations.Team;
import se.tink.api.annotations.TeamOwnership;
import se.tink.libraries.http.annotations.auth.AllowAnonymous;

@Path("/aggregation/controller")
public interface AggregationControllerService {
    @GET
    @Path("ping")
    @TeamOwnership(Team.INTEGRATION)
    @Produces(MediaType.TEXT_PLAIN)
    @AllowAnonymous
    String ping();
}
