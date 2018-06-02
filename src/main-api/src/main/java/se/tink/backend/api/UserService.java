package se.tink.backend.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import se.tink.api.annotations.Team;
import se.tink.api.annotations.TeamOwnership;
import se.tink.backend.auth.Authenticated;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.auth.AuthenticationContext;
import se.tink.backend.auth.EnrichRequest;
import se.tink.backend.auth.OAuth2ClientRequest;
import se.tink.backend.core.Market;
import se.tink.backend.core.User;
import se.tink.backend.core.UserConnectedServiceTypes;
import se.tink.backend.core.UserContext;
import se.tink.backend.core.UserLocation;
import se.tink.backend.core.UserOrigin;
import se.tink.backend.core.UserProfile;
import se.tink.backend.core.oauth2.OAuth2AuthorizationScopeTypes;
import se.tink.backend.rpc.AnonymousUserRequest;
import se.tink.backend.rpc.AnonymousUserResponse;
import se.tink.backend.rpc.DeleteUserRequest;
import se.tink.backend.rpc.MarketListResponse;
import se.tink.backend.rpc.TinkMediaType;
import se.tink.backend.rpc.UpdateUserProfileDataRequest;
import se.tink.backend.rpc.UserLoginResponse;
import se.tink.backend.utils.ApiTag;

@Path("/api/v1/user")
@Api(value = ApiTag.USER_SERVICE, description = "User Service handles operations regarding the User")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface UserService {

    String PASSWORD_CONFIRMATION_HEADER_NAME = "X-Tink-Password-Confirmation";
    String TOKEN_HEADER_NAME = "X-Tink-Token";

    /**
     * Helper function to confirm the user's password.
     */
    @POST
    @Path("confirm")
    @TeamOwnership(Team.PFM)
    @ApiOperation(value = "", hidden = true)
    void confirm(
            @Authenticated @ApiParam(hidden = true) AuthenticatedUser authenticatedUser,
            @HeaderParam(PASSWORD_CONFIRMATION_HEADER_NAME) String passwordConfirmation
    );

    /**
     * Completely deletes the user and all the user's data.
     *
     * @param deleteUserRequest The reasons why the user chose to delete his account.
     */
    @POST
    @Path("delete")
    @Consumes({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @TeamOwnership(Team.PFM)
    @ApiOperation(value = "", hidden = true)
    void delete(
            @Authenticated @ApiParam(hidden = true) AuthenticatedUser authenticatedUser,
            DeleteUserRequest deleteUserRequest
    );

    /**
     * Initiates a forgotten password process by sending out a password reset token to the user's registered email - but
     * only if the user has verified his address.
     *
     * @param user The user (only username).
     */
    @POST
    @Path("forgot")
    @TeamOwnership(Team.PFM)
    @Consumes({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @ApiOperation(value = "", hidden = true)
    void forgotPassword(User user);

    /**
     * Get the user.
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @ApiOperation(
            value = "Get the user",
            notes = "Returns the user object. Note that the password field is not stored in clear text nor populated when getting the user. It's only used for setting the password when registering a new user.",
            response = User.class
    )
    @TeamOwnership(Team.PFM)
    User getUser(
            @Authenticated(scopes = { OAuth2AuthorizationScopeTypes.USER_READ }) @ApiParam(hidden = true) User user
    );

    /**
     * Gets the user context.
     *
     * @throws WebApplicationException NOT_MODIFIED if the context has not been modified since last request
     * @throws WebApplicationException INTERNAL_SERVER_ERROR if it hit the fan...
     */
    @GET
    @Path("context")
    @TeamOwnership(Team.PFM)
    @Produces({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @ApiOperation(value = "", hidden = true)
    UserContext getContext(@Authenticated @ApiParam(hidden = true) AuthenticatedUser authenticatedUser);

    /**
     * Gets the user's profile.
     *
     * @throws WebApplicationException BAD_REQUEST if the session has no user attached to it
     */
    @GET
    @Path("profile")
    @Produces({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @ApiOperation(
            value = "Get the user profile",
            notes = "Returns the user profile.",
            response = UserProfile.class
    )
    @TeamOwnership(Team.PFM)
    UserProfile getProfile(@Authenticated @ApiParam(hidden = true) User user);

    /**
     * Link an external service to the Tink user.
     */
    @POST
    @Path("services/link")
    @Consumes({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @ApiOperation(value = "", hidden = true)
    @TeamOwnership(Team.GROWTH)
    void linkService(
            @Authenticated @ApiParam(hidden = true) User user,
            @QueryParam("type") UserConnectedServiceTypes type,
            @QueryParam("token") String token
    );

    /**
     * Get a list of the supported markets
     *
     * @param desiredMarketCode Optional parameter with a market (country ISO code) that the client prefers.
     */
    @GET
    @Path("markets")
    @Produces({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @ApiOperation(value = "", hidden = true)
    @TeamOwnership(Team.PFM)
    List<Market> listMarkets(@QueryParam("desired") String desiredMarketCode);

    /**
     * Get a list of the supported markets
     *
     * @param desiredMarketCode Optional parameter with a market (country ISO code) that the client prefers.
     */
    @GET
    @Path("markets/list")
    @Produces({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @ApiOperation(
            value = "List markets",
            notes = "Returns an object with a list of all available markets in which a user could register with.",
            response = MarketListResponse.class
    )
    @TeamOwnership(Team.PFM)
    MarketListResponse getMarketList(
            @QueryParam("desired") @ApiParam(
                    value = "The ISO 3166-1 alpha-2 country code of the desired market",
                    example = "FI") String desiredMarketCode
    );

    /**
     * Login.
     *
     * @throws WebApplicationException BAD_REQUEST if no user was passed, or username or password was null or empty
     * @throws WebApplicationException UNAUTHORIZED if invalid credentials
     */
    @POST
    @Path("login")
    @Produces({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @Consumes({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @ApiOperation(value = "", hidden = true)
    @TeamOwnership(Team.PFM)
    UserLoginResponse login(
            @Authenticated(required = false) @ApiParam(hidden = true) AuthenticatedUser authenticatedUser,
            @EnrichRequest OAuth2ClientRequest oauth2ClientRequest,
            User loginUser
    );

    /**
     * Logout.
     */
    @POST
    @Path("logout")
    @Consumes({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @ApiOperation(value = "", hidden = true)
    @TeamOwnership(Team.PFM)
    void logout(
            @Authenticated @ApiParam(hidden = true) AuthenticatedUser authenticatedUser,
            @QueryParam("autologout") boolean autologout
    );

    /**
     * Pings the system for the load balancer to know the node is ok.
     */
    @GET
    @Path("ping")
    @Produces({ MediaType.TEXT_PLAIN })
    @ApiOperation(value = "", hidden = true)
    @TeamOwnership(Team.PFM)
    String ping(@QueryParam("service") String service);

    /**
     * Poll the user context.
     */
    @GET
    @Path("context/poll")
    @Produces({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @ApiOperation(value = "", hidden = true)
    @TeamOwnership(Team.PFM)
    UserContext pollContext(
            @Authenticated @ApiParam(hidden = true) AuthenticatedUser authenticatedUser,
            @QueryParam("contextTimestamp") Long contextTimestamp,
            @QueryParam("statisticsTimestamp") Long statisticsTimestamp,
            @QueryParam("activitiesTimestamp") Long activitiesTimestamp
    );

    /**
     * Register a new user.
     *
     * @param registerUser The user's credentials.
     * @throws WebApplicationException BAD_REQUEST if no user was passed, or the supplied email is invalid
     * @throws WebApplicationException UNAUTHORIZED if no valid invite code was supplied
     * @throws WebApplicationException CONFLICT if email is already registered
     */
    @POST
    @Path("register")
    @Produces({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @Consumes({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @ApiOperation(value = "", hidden = true)
    @TeamOwnership(Team.PFM)
    UserLoginResponse register(User registerUser);

    /**
     * Register a new user anonymously. This user will not have an email.
     *
     * @throws WebApplicationException BAD_REQUEST if no user was passed, or the supplied email is invalid
     * @throws WebApplicationException UNAUTHORIZED if no valid invite code was supplied
     * @throws WebApplicationException CONFLICT if email is already registered
     */
    @POST
    @Path("register/anonymous")
    @Produces({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @Consumes({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @ApiOperation(value = "", hidden = true)
    @TeamOwnership(Team.GROWTH)
    AnonymousUserResponse registerAnonymous(
            @EnrichRequest OAuth2ClientRequest oauth2ClientRequest,
            AnonymousUserRequest request
    );

    /**
     * Report user location.
     */
    @POST
    @Path("location")
    @Consumes({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @ApiOperation(value = "", tags = { ApiTag.USER_SERVICE, ApiTag.HIDE })
    @TeamOwnership(Team.PFM)
    void reportLocation(@Authenticated @ApiParam(hidden = true) User user, UserLocation location);

    /**
     * Receiving endpoint for CSP policy violations.
     */
    @POST
    @Path("policy/report")
    @ApiOperation(value = "", hidden = true)
    @TeamOwnership(Team.PFM)
    void reportPolicy(String data);

    /**
     * Resets the user's password by supplying the token previously sent out.
     *
     * @param token The token which the user should have received in a mail.
     * @param user  The user's new credentials.
     */
    @POST
    @Path("reset")
    @Consumes({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @ApiOperation(value = "", hidden = true)
    @TeamOwnership(Team.PFM)
    void resetPassword(@HeaderParam(TOKEN_HEADER_NAME) String token, User user);

    /**
     * Updates the user's credentials.
     *
     * @param updatedUser          The user's updated credentials.
     * @param passwordConfirmation The user's current password (for confirmation).
     */
    @PUT
    @Produces({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @ApiOperation(
            value = "Update the user",
            notes = "Updates certain user modifiable properties of a user. Please refer to the body schema to see which properties are modifiable by the user.",
            response = User.class,
            tags = { ApiTag.USER_SERVICE, ApiTag.HIDE }
    )
    @TeamOwnership(Team.PFM)
    User updateUser(
            @Authenticated @ApiParam(hidden = true) AuthenticatedUser authenticatedUser,
            @ApiParam(value = "The updated user object", required = true) User updatedUser,
            @HeaderParam(PASSWORD_CONFIRMATION_HEADER_NAME) @ApiParam(hidden = true) String passwordConfirmation
    );

    /**
     * Updates the user's profile.
     *
     * @param profile The user's updated profile.
     * @throws WebApplicationException BAD_REQUEST if the session has no user attached to it
     */
    @PUT
    @Path("profile")
    @Produces({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @Consumes({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @ApiOperation(
            value = "Update the user profile",
            notes = "Updates certain user modifiable properties of a user's profile. Please refer to the body schema to see which properties are modifiable by the user.",
            response = UserProfile.class,
            tags = { ApiTag.USER_SERVICE, ApiTag.HIDE }
    )
    @TeamOwnership(Team.PFM)
    UserProfile updateProfile(
            @Authenticated @ApiParam(hidden = true) User user,
            @ApiParam(value = "The updated user profile object", required = true) UserProfile profile
    );

    /**
     * Updates the user's profile fields.
     */
    @PUT
    @Path("profile/data")
    @Produces({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @Consumes({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @ApiOperation(value = "", hidden = true)
    @TeamOwnership(Team.PFM)
    void updateProfileData(@Authenticated @ApiParam(hidden = true) User user, UpdateUserProfileDataRequest profile);

    /**
     * Sets the user's origin
     */
    @POST
    @Path("origin")
    @Produces({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @Consumes({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @ApiOperation(value = "", hidden = true)
    @TeamOwnership(Team.PFM)
    void setOrigin(@Authenticated @ApiParam(hidden = true) User user, UserOrigin origin);

    /**
     * Endpoint to track user open links.
     */
    @GET
    @Path("open{path:.*}")
    @ApiOperation(value = "", hidden = true)
    @TeamOwnership(Team.PFM)
    Response open();

    @POST
    @Path("twilio")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @ApiOperation(value = "", hidden = true)
    @TeamOwnership(Team.PFM)
    Response sendStoreLinkAsSms(@FormParam("phoneNumber") String phoneNumber);

    @POST
    @Path("rate")
    @ApiOperation(value = "", hidden = true)
    @TeamOwnership(Team.PFM)
    Response rateThisApp(
            @Authenticated @ApiParam(hidden = true) AuthenticationContext authenticationContext,
            @QueryParam("status") String status
    );
}
