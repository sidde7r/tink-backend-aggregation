package se.tink.backend.aggregation.aggregationcontroller.v1.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import se.tink.api.annotations.Team;
import se.tink.api.annotations.TeamOwnership;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateCredentialsSensitiveRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.credentialsservice.UpdateCredentialsSupplementalInformationRequest;

@Path("/aggregation/controller/v1/credentials")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface CredentialsService {
    @PUT
    @Path("/sensitive")
    @TeamOwnership(Team.AGGREGATION)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response updateSensitive(UpdateCredentialsSensitiveRequest request);

    @PUT
    @Path("/supplementalInformation")
    @TeamOwnership(Team.AGGREGATION)
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    Response updateCredentialsSupplementalInformation(
            UpdateCredentialsSupplementalInformationRequest request);
}
