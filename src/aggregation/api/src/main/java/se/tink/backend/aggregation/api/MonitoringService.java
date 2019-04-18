package se.tink.backend.aggregation.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import se.tink.libraries.api.annotations.Team;
import se.tink.libraries.api.annotations.TeamOwnership;
import se.tink.libraries.http.annotations.auth.AllowAnonymous;

@Path("/monitoring")
public interface MonitoringService {
    @GET
    @Path("connectivity/")
    @TeamOwnership(Team.AGGREGATION)
    @Produces(MediaType.APPLICATION_JSON)
    @AllowAnonymous
    Response checkConnectivity();

    @GET
    @Path("connectivity/{clusterId}")
    @TeamOwnership(Team.AGGREGATION)
    @Produces(MediaType.APPLICATION_JSON)
    @AllowAnonymous
    Response checkConnectivity(@PathParam("clusterId") String clusterId);
}
