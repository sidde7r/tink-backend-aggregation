package se.tink.backend.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import se.tink.api.annotations.Team;
import se.tink.api.annotations.TeamOwnership;
import se.tink.libraries.http.annotations.auth.AllowAnonymous;

@Path("/api/v1/monitoring")
@Api(value = "Monitoring Service")
public interface MonitoringService {
    
    @GET
    @Path("healthy")
    @TeamOwnership(Team.PFM)
    @AllowAnonymous
    @ApiOperation(value = "Health check",
            notes = "Returns `ok` while the Tink API isn't experiencing unexpected disturbances. Returns `503 Service Unavailable` if the server is closing down and `500 Internal Server Error` for any other error.")
    @Produces({
        MediaType.TEXT_PLAIN
    })
    String healthy();

    @GET
    @Path("ping")
    @TeamOwnership(Team.PFM)
    @AllowAnonymous
    @ApiOperation(value = "Ping",
            notes = "Checks the current status of a Tink service and returns `pong` if the specified service is running.")
    @Produces({
        MediaType.TEXT_PLAIN
    })
    String ping(@QueryParam("service") @ApiParam(name = "service", value = "Forwards the ping to another service (\"aggregation\", \"main\" and \"system\"). Defaults to \"main\".", allowableValues = "aggregation, system, main", defaultValue = "main")
            String service);
}
