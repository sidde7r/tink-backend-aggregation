package se.tink.backend.aggregation.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import se.tink.libraries.api.annotations.Team;
import se.tink.libraries.api.annotations.TeamOwnership;

@Path("/monitoring")
public interface MonitoringService {

    @GET
    @Path("connectivity/{clusterId}")
    @TeamOwnership(Team.INTEGRATION)
    @Produces(MediaType.TEXT_PLAIN)
    String checkConnectivity(@PathParam("clusterId") String clusterId);
}
