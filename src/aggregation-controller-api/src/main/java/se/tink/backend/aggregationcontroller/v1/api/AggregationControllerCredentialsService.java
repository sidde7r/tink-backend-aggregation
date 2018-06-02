package se.tink.backend.aggregationcontroller.v1.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import se.tink.api.annotations.Team;
import se.tink.api.annotations.TeamOwnership;
import se.tink.backend.aggregationcontroller.v1.rpc.credentialsservice.UpdateCredentialsSensitiveRequest;

@Path("/aggregation/controller/v1/credentials")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface AggregationControllerCredentialsService {
    @PUT
    @Path("/sensitive")
    @TeamOwnership(Team.INTEGRATION)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response updateSensitive(UpdateCredentialsSensitiveRequest request);
}
