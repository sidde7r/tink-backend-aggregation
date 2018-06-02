package se.tink.backend.insights.http;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import se.tink.api.annotations.Team;
import se.tink.api.annotations.TeamOwnership;
import se.tink.libraries.http.annotations.auth.AllowAnonymous;

@Path("/insights/monitor/")
public interface InsightsMonitoringService {
    @GET
    @Path("/ping")
    @TeamOwnership(Team.PFM)
    @Produces({
            MediaType.TEXT_PLAIN
    })
    @AllowAnonymous
    String ping();
}
