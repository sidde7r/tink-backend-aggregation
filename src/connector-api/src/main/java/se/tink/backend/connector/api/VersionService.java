package se.tink.backend.connector.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import se.tink.api.annotations.Team;
import se.tink.api.annotations.TeamOwnership;
import se.tink.backend.connector.rpc.VersionResponse;
import se.tink.libraries.http.annotations.auth.AllowAnonymous;
import se.tink.libraries.http.annotations.auth.AllowClient;

@Path("/version")
@Api(value = "Version Service", description = "Version Service exposes the current version of the application")
@AllowClient("SEB")
public interface VersionService {
    @GET
    @Produces({
            MediaType.APPLICATION_JSON
    })
    @ApiOperation(value = "Get the version",
            notes = "Gets the current version (build) of the application",
            response = VersionResponse.class
    )
    @AllowAnonymous
    @TeamOwnership(Team.DATA)
    VersionResponse getVersion();
}
