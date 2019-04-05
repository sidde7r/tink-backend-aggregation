package se.tink.backend.aggregation.aggregationcontroller.v1.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import se.tink.libraries.api.annotations.Team;
import se.tink.libraries.api.annotations.TeamOwnership;

@Path("/aggregation/controller")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface AggregationControllerService {

    @POST
    @Path("connectivity")
    @TeamOwnership(Team.INTEGRATION)
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    Response connectivityCheck();
}
