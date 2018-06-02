package se.tink.backend.api;

import java.util.UUID;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import se.tink.api.annotations.Team;
import se.tink.api.annotations.TeamOwnership;
import se.tink.backend.auth.Authenticated;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.rpc.TinkMediaType;

@Path("/api/v1/documents")
public interface DocumentService {

    @GET
    @Path("{userId}/{token}")
    @TeamOwnership(Team.PFM)
    @Produces({
            TinkMediaType.APPLICATION_PDF
    })
    Response getDocument(
            @Authenticated(required = false) AuthenticatedUser user,
            @PathParam("userId") String userId,
            @PathParam("token") UUID token);
}
