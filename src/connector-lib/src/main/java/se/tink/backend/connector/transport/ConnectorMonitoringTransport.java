package se.tink.backend.connector.transport;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import se.tink.api.annotations.Team;
import se.tink.api.annotations.TeamOwnership;
import se.tink.libraries.http.annotations.auth.AllowAnonymous;

@Path("/connector/monitoring")
public class ConnectorMonitoringTransport {

    @GET
    @Path("ping")
    @TeamOwnership(Team.DATA)
    @AllowAnonymous
    @Produces(MediaType.TEXT_PLAIN)
    public String ping() {
        return "pong";
    }
}
