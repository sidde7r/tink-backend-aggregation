package se.tink.backend.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import se.tink.api.annotations.Team;
import se.tink.api.annotations.TeamOwnership;
import se.tink.backend.auth.Authenticated;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.auth.EnrichRequest;
import se.tink.backend.auth.OAuth2ClientRequest;
import se.tink.backend.core.User;
import se.tink.backend.core.UserDevice;
import se.tink.backend.core.oauth2.OAuth2AuthenticationTokenResponse;
import se.tink.backend.core.oauth2.OAuth2AuthorizationDescription;
import se.tink.backend.core.oauth2.OAuth2AuthorizeRequest;
import se.tink.backend.core.oauth2.OAuth2AuthorizeResponse;
import se.tink.backend.core.oauth2.OAuth2Client;
import se.tink.backend.core.oauth2.OAuth2ClientProductMetaData;
import se.tink.backend.exception.jersey.JerseyRequestException;
import se.tink.backend.rpc.OAuth2ClientListResponse;
import se.tink.backend.rpc.TinkMediaType;
import se.tink.backend.rpc.oauth.OAuthPartnerRequest;

@Path("/api/v1/oauth")
@Api(value = "OAuth Service", description = "Access to the Tink API for 3rd party API consumers is available using industry standard OAuth2 authentication methods for transparent and secure access to user data.")
public interface OAuth2Service {
    @POST
    @Produces({
            MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF
    })
    @Consumes({
            MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF
    })
    @Path("authorize")
    @TeamOwnership(Team.GROWTH)
    @ApiOperation(value = "", hidden = true)
    OAuth2AuthorizeResponse authorize(@Authenticated User user, OAuth2AuthorizeRequest request);

    @POST
    @Produces({
            MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF
    })
    @Consumes({
            MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF
    })
    @Path("auto-authorize")
    @TeamOwnership(Team.GROWTH)
    @ApiOperation(value = "", hidden = true)
    OAuth2AuthorizeResponse autoAuthorize(@Authenticated User user, OAuth2AuthorizeRequest request);

    @POST
    @Produces({
            MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
    })
    @Path("token")
    @TeamOwnership(Team.GROWTH)
    @ApiOperation(value = "Get an authorization token", notes = "Exchange a code or a refresh token for an authorization token. The authorization token can later be used to access API resources.")
    OAuth2AuthenticationTokenResponse token(@FormParam("client_id") String clientId,
            @FormParam("client_secret") String clientSecret, @FormParam("grant_type") String grantType,
            @FormParam("code") String code, @FormParam("refresh_token") String refreshToken)
            throws JerseyRequestException;

    @POST
    @Produces({
            MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF
    })
    @Consumes({
            MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF
    })
    @Path("describe")
    @TeamOwnership(Team.GROWTH)
    @ApiOperation(value = "", hidden = true)
    OAuth2AuthorizationDescription describe(@Authenticated User user, OAuth2AuthorizeRequest request)
            throws JerseyRequestException;

    @GET
    @Produces({
            MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF
    })
    @Path("product-meta-data")
    @TeamOwnership(Team.GROWTH)
    @ApiOperation(value = "", hidden = true)
    OAuth2ClientProductMetaData getOAuth2ClientProductData(@Authenticated(required = false) AuthenticatedUser user,
            @EnrichRequest OAuth2ClientRequest oauth2ClientRequest);

    @Produces({
            MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF
    })
    @Consumes({
            MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF
    })
    @POST
    @Path("devices/{id}/authorize")
    @TeamOwnership(Team.GROWTH)
    @ApiOperation(value = "", hidden = true)
    UserDevice authorizeUserDevice(@Authenticated(requireAuthorizedDevice = false) User user, @PathParam("id") String deviceId);

    @Produces({
            MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF
    })
    @Consumes({
            MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF
    })
    @GET
    @Path("devices/{id}")
    @TeamOwnership(Team.GROWTH)
    @ApiOperation(value = "", hidden = true)
    UserDevice getUserDevice(@Authenticated(requireAuthorizedDevice = false) User user, @PathParam("id") String deviceId);

    @Produces({
            MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF
    })
    @Consumes({
            MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF
    })
    @GET
    @Path("manager/client")
    @TeamOwnership(Team.GROWTH)
    @ApiOperation(value = "", hidden = true)
    OAuth2ClientListResponse getOauthClients(
            @Authenticated(requireAuthorizedDevice = false) AuthenticatedUser user);

    @Produces({
            MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF
    })
    @Consumes({
            MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF
    })
    @PUT
    @Path("manager/client/{id}")
    @TeamOwnership(Team.GROWTH)
    @ApiOperation(value = "", hidden = true)
    OAuth2Client updateOauthClient(@Authenticated(requireAuthorizedDevice = false) AuthenticatedUser user,
            @PathParam("id") String id, @Valid OAuth2Client incomingClient) throws JerseyRequestException;

    @POST
    @Path("temporary/partner/signup")
    @TeamOwnership(Team.GROWTH)
    @ApiOperation(value = "", hidden = true)
    void registerNewOAuthClientForNewPartner(@Valid OAuthPartnerRequest request) throws JerseyRequestException;
}
