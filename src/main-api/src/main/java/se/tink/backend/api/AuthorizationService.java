package se.tink.backend.api;

import io.swagger.annotations.ApiOperation;
import java.util.List;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import se.tink.api.annotations.Team;
import se.tink.api.annotations.TeamOwnership;
import se.tink.backend.auth.Authenticated;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.core.oauth2.OAuth2Authorization;

@Path("/api/v1/authorization")
public interface AuthorizationService {

    @DELETE
    @Path("/{id}")
    @TeamOwnership(Team.GROWTH)
    @ApiOperation(value = "", hidden = true)
    public void delete(@Authenticated AuthenticatedUser authenticatedUser, @PathParam("id") String id);

    @GET
    @Path("/{id}")
    @TeamOwnership(Team.GROWTH)
    @Produces({
        MediaType.APPLICATION_JSON
    })
    @ApiOperation(value = "", hidden = true)
    public OAuth2Authorization get(@Authenticated AuthenticatedUser authenticatedUser, @PathParam("id") String id);

    @GET
    @Produces({
        MediaType.APPLICATION_JSON
    })
    @TeamOwnership(Team.GROWTH)
    @ApiOperation(value = "", hidden = true)
    public List<OAuth2Authorization> list(@Authenticated AuthenticatedUser authenticatedUser);
}
