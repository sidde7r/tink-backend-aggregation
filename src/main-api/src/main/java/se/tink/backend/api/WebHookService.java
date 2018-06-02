package se.tink.backend.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import se.tink.api.annotations.Team;
import se.tink.api.annotations.TeamOwnership;
import se.tink.backend.auth.Authenticated;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.core.oauth2.OAuth2AuthorizationScopeTypes;
import se.tink.backend.core.oauth2.OAuth2WebHook;
import se.tink.backend.rpc.OAuth2WebHookResponse;

@Path("/api/v1/authorization")
@Api(value = "Web Hook Service", description = "A service for setting up web hooks.")
public interface WebHookService {

    @GET
    @Path("hooks")
    @TeamOwnership(Team.DATA)
    @Produces({
            MediaType.APPLICATION_JSON
    })
    OAuth2WebHookResponse listHooks(@Authenticated(scopes = {
            OAuth2AuthorizationScopeTypes.USER_WEB_HOOKS
    }) AuthenticatedUser authenticatedUser);

    @POST
    @Path("hooks")
    @TeamOwnership(Team.DATA)
    @Produces({
            MediaType.APPLICATION_JSON
    })
    @Consumes({
            MediaType.APPLICATION_JSON
    })
    @ApiOperation(value = "Create Web Hook",
            notes = "Create a new web hook for the authenticated user, giving the possibility to get pushed updates for certain events.",
            response = OAuth2WebHook.class
    )
    OAuth2WebHook createHook(@Authenticated(scopes = {
            OAuth2AuthorizationScopeTypes.USER_WEB_HOOKS
    }) AuthenticatedUser authenticatedUser,
            @ApiParam(value = "The specifics of the web hook to create", required = true)
                    se.tink.backend.rpc.webhook.OAuth2WebHook hook);

    @DELETE
    @Path("hooks/{id}")
    @TeamOwnership(Team.DATA)
    @ApiOperation(value = "Delete Web Hook",
            notes = "Delete the web hook of the given id."
    )
    void deleteHook(@Authenticated(scopes = {
            OAuth2AuthorizationScopeTypes.USER_WEB_HOOKS
    }) AuthenticatedUser authenticatedUser,
            @ApiParam(value = "The id of the web hook", required = true) @PathParam("id") String id);
}
