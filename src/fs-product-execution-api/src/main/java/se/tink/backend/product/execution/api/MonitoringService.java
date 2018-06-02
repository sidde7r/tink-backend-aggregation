package se.tink.backend.product.execution.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import se.tink.api.annotations.Team;
import se.tink.api.annotations.TeamOwnership;
import se.tink.libraries.http.annotations.auth.AllowAnonymous;


@Path("/product/executor/monitor")
public interface MonitoringService {
    @GET
    @Path("/ping")
    @TeamOwnership(Team.FINANCIAL_SERVICES)
    @Produces(MediaType.TEXT_PLAIN)
    @AllowAnonymous
    String ping();
}
