package se.tink.backend.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.List;
import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import se.tink.api.annotations.Team;
import se.tink.api.annotations.TeamOwnership;
import se.tink.backend.rpc.RefreshableItem;
import se.tink.backend.auth.Authenticated;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.auth.AuthenticationContext;
import se.tink.backend.auth.EnrichRequest;
import se.tink.backend.auth.OAuth2ClientRequest;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Provider;
import se.tink.backend.core.oauth2.OAuth2AuthorizationScopeTypes;
import se.tink.backend.rpc.CredentialsListResponse;
import se.tink.backend.rpc.ProviderListResponse;
import se.tink.backend.rpc.RefreshCredentialsRequest;
import se.tink.backend.rpc.SignableOperationsResponse;
import se.tink.backend.rpc.SupplementalInformation;
import se.tink.backend.rpc.TinkMediaType;
import se.tink.backend.utils.ApiTag;

@Path("/api/v1/credentials")
@Api(value = ApiTag.CREDENTIALS_SERVICE,
        description = "Credentials refers to the set of information required to refresh data from one Provider.")
public interface CredentialsService {

    @POST
    @Consumes({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @Produces({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @ApiOperation(
            value = "Create credentials",
            notes = "Creates the Credentials for the user. The create request will trigger a refresh towards the provider.",
            response = Credentials.class,
            tags = { ApiTag.CREDENTIALS_SERVICE, ApiTag.HIDE }
    )
    @TeamOwnership(Team.INTEGRATION)
    Credentials create(
            @Authenticated(scopes = { OAuth2AuthorizationScopeTypes.CREDENTIALS_WRITE })
            @ApiParam(hidden = true) AuthenticatedUser authenticatedUser,
            @EnrichRequest OAuth2ClientRequest oauth2ClientRequest,
            @ApiParam(value = "The credentials to create. Only providerName and fields are required.") Credentials credentials,
            @QueryParam("items") Set<RefreshableItem> refreshableItems
    );

    @DELETE
    @Path("{id}")
    @TeamOwnership(Team.INTEGRATION)
    @ApiOperation(
            value = "Delete credentials",
            notes = "Deletes the given credentials.",
            tags = { ApiTag.CREDENTIALS_SERVICE, ApiTag.HIDE }
    )
    void delete(
            @Authenticated(scopes = { OAuth2AuthorizationScopeTypes.CREDENTIALS_WRITE })
            @ApiParam(hidden = true) AuthenticatedUser authenticatedUser,
            @PathParam("id") @ApiParam(value = "The id of the credentials", required = true) String id
    );

    @GET
    @Path("/{id}")
    @TeamOwnership(Team.INTEGRATION)
    @Produces({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @ApiOperation(value = "Get credentials", notes = "Gets credentials by id.", response = Credentials.class)
    Credentials get(
            @Authenticated(scopes = { OAuth2AuthorizationScopeTypes.CREDENTIALS_READ })
            @ApiParam(hidden = true) AuthenticatedUser authenticatedUser,
            @PathParam("id") @ApiParam(value = "The id of the credentials", required = true) String id
    );

    @PUT
    @Path("/{id}")
    @TeamOwnership(Team.INTEGRATION)
    @Consumes({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @Produces({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @ApiOperation(
            value = "Update credentials",
            notes = "Updates the specified credentials.",
            response = Credentials.class,
            tags = { ApiTag.CREDENTIALS_SERVICE, ApiTag.HIDE }
    )
    Credentials update(
            @Authenticated(scopes = { OAuth2AuthorizationScopeTypes.CREDENTIALS_WRITE })
            @ApiParam(hidden = true) AuthenticatedUser authenticatedUser,
            @PathParam("id") @ApiParam(value = "The id of the credentials", required = true) String id,
            @ApiParam(value = "The new credentials object", required = true) Credentials credentials
    );

    @GET
    @Path("/{id}/operations")
    @TeamOwnership(Team.INTEGRATION)
    @Produces({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @ApiOperation(value = "", hidden = true)
    SignableOperationsResponse getSignableOperations(
            @Authenticated(scopes = { OAuth2AuthorizationScopeTypes.CREDENTIALS_READ })
            @ApiParam(hidden = true) AuthenticatedUser authenticatedUser, @PathParam("id") String credentialsId
    );

    /**
     * External users shouldn't use this method anymore. Use /credentials/list => getCredentialsList (User)
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @TeamOwnership(Team.INTEGRATION)
    @ApiOperation(value = "", hidden = true)
    List<Credentials> list(
            @Authenticated(scopes = { OAuth2AuthorizationScopeTypes.CREDENTIALS_READ })
            @ApiParam(hidden = true) AuthenticatedUser user
    );

    @GET
    @Path("/list")
    @TeamOwnership(Team.INTEGRATION)
    @Produces({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @ApiOperation(
            value = "List credentials",
            notes = "List all credentials for the user.",
            response = CredentialsListResponse.class
    )
    CredentialsListResponse getCredentialsList(
            @Authenticated(scopes = { OAuth2AuthorizationScopeTypes.CREDENTIALS_READ })
            @ApiParam(hidden = true) AuthenticatedUser user
    );

    @GET
    @Path("/provider/{name}")
    @TeamOwnership(Team.INTEGRATION)
    @Produces({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @ApiOperation(value = "", hidden = true)
    Provider getProvider(
            @Authenticated(scopes = { OAuth2AuthorizationScopeTypes.CREDENTIALS_READ })
            @ApiParam(hidden = true) AuthenticatedUser authenticatedUser,
            @PathParam("name") String name
    );

    /**
     * External users shouldn't use this method anymore. Use GET /providers
     */
    @GET
    @Path("/providers")
    @TeamOwnership(Team.INTEGRATION)
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "", hidden = true)
    @Deprecated
    List<Provider> listProviders(
            @Authenticated(scopes = { OAuth2AuthorizationScopeTypes.CREDENTIALS_READ })
            @ApiParam(hidden = true) AuthenticatedUser user
    );

    /**
     * External users shouldn't use this method anymore. Use GET /providers
     */
    @GET
    @Path("/providers/list")
    @TeamOwnership(Team.INTEGRATION)
    @Produces({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @ApiOperation(value = "", hidden = true)
    @Deprecated
    ProviderListResponse getProvidersList(
            @Authenticated(scopes = { OAuth2AuthorizationScopeTypes.CREDENTIALS_READ })
            @ApiParam(hidden = true) AuthenticatedUser user
    );

    /**
     * External users shouldn't use this method anymore. Use GET /providers/{market}
     */
    @GET
    @Path("/providers/list/{market}")
    @TeamOwnership(Team.INTEGRATION)
    @Produces({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @ApiOperation(value = "", hidden = true)
    @Deprecated
    ProviderListResponse getProvidersByMarket(
            @Authenticated(required = false) AuthenticationContext authenticationContext,
            @PathParam("market") String market
    );

    @POST
    @Path("/{id}/refresh")
    @TeamOwnership(Team.INTEGRATION)
    @Consumes({ MediaType.APPLICATION_JSON })
    @ApiOperation(
            value = "Refresh credentials",
            notes = "Refreshes the specified credentials.",
            tags = { ApiTag.CREDENTIALS_SERVICE, ApiTag.HIDE }
    )
    void refresh(
            @Authenticated(scopes = { OAuth2AuthorizationScopeTypes.CREDENTIALS_REFRESH })
            @ApiParam(hidden = true) AuthenticatedUser authenticatedUser,
            @PathParam("id") @ApiParam(value = "The internal identifier of the Credentials object to refresh.",
                    required = true, example = "2d3bd65493b549e1927d97a2d0683ab9") String id,
            @QueryParam("items") Set<RefreshableItem> refreshableItems
    );

    @POST
    @Path("/refresh")
    @TeamOwnership(Team.INTEGRATION)
    @Consumes({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @Produces({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
    @ApiOperation(value = "", hidden = true)
    void refresh(
            @Authenticated(scopes = { OAuth2AuthorizationScopeTypes.CREDENTIALS_REFRESH })
            @ApiParam(hidden = true) AuthenticatedUser authenticatedUser,
            RefreshCredentialsRequest request,
            @QueryParam("items") Set<RefreshableItem> refreshableItems
    );

    @POST
    @Path("/{id}/supplement")
    @TeamOwnership(Team.INTEGRATION)
    @Consumes({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "", hidden = true)
    void supplement(
            @Authenticated(scopes = { OAuth2AuthorizationScopeTypes.CREDENTIALS_REFRESH })
            @ApiParam(hidden = true) AuthenticatedUser authenticatedUser,
            @PathParam("id") String id, String information
    );

    @POST
    @Path("/{id}/supplemental-information")
    @TeamOwnership(Team.INTEGRATION)
    @Consumes({ MediaType.APPLICATION_JSON })
    @ApiOperation(
            value = "Add Supplemental Information",
            notes = "Adds supplemental information to an authentication.",
            tags = { ApiTag.CREDENTIALS_SERVICE, ApiTag.HIDE }
    )
    void supplemental(
            @Authenticated(scopes = { OAuth2AuthorizationScopeTypes.CREDENTIALS_REFRESH })
            @ApiParam(hidden = true) AuthenticatedUser authenticatedUser, @PathParam("id") String id,
            @ApiParam(value = "The supplemental information.",
                    required = true) SupplementalInformation supplementalInformation
    );

    @POST
    @Path("/{id}/disable")
    @TeamOwnership(Team.INTEGRATION)
    @ApiOperation(value = "", hidden = true)
    void disable(
            @Authenticated(scopes = { OAuth2AuthorizationScopeTypes.CREDENTIALS_WRITE })
            @ApiParam(hidden = true) AuthenticatedUser authenticatedUser,
            @PathParam("id") String id
    );

    @POST
    @Path("/{id}/enable")
    @TeamOwnership(Team.INTEGRATION)
    @ApiOperation(value = "", hidden = true)
    void enable(
            @Authenticated(scopes = { OAuth2AuthorizationScopeTypes.CREDENTIALS_WRITE })
            @ApiParam(hidden = true) AuthenticatedUser authenticatedUser,
            @PathParam("id") String id
    );

    @POST
    @Path("/{id}/keepalive")
    @TeamOwnership(Team.INTEGRATION)
    @ApiOperation(value = "", hidden = true)
    void keepAlive(
            @Authenticated(scopes = { OAuth2AuthorizationScopeTypes.CREDENTIALS_REFRESH })
            @ApiParam(hidden = true) AuthenticatedUser authenticatedUser,
            @PathParam("id") String id
    );
}
