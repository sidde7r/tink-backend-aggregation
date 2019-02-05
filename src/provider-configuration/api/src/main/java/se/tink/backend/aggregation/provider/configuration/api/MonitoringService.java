package se.tink.backend.aggregation.provider.configuration.api;


import se.tink.backend.libraries.api.annotations.Team;
import se.tink.backend.libraries.api.annotations.TeamOwnership;
import se.tink.libraries.http.annotations.auth.AllowAnonymous;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/monitoring")
public interface MonitoringService {
    @GET
    @Path("/ping")
    @TeamOwnership(Team.INTEGRATION)
    @Produces(MediaType.TEXT_PLAIN)
    @AllowAnonymous
    String ping();
}
