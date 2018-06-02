package se.tink.backend.main.transports;

import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import se.tink.api.annotations.Team;
import se.tink.api.annotations.TeamOwnership;
import se.tink.backend.api.ProviderService;
import se.tink.backend.auth.Authenticated;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.auth.AuthenticationContext;
import se.tink.backend.core.Provider;
import se.tink.backend.core.oauth2.OAuth2AuthorizationScopeTypes;
import se.tink.backend.core.oauth2.OAuth2Client;
import se.tink.backend.core.oauth2.OAuth2Utils;
import se.tink.backend.main.controllers.ProviderServiceController;
import se.tink.backend.rpc.ProviderListResponse;
import se.tink.backend.rpc.TinkMediaType;
import se.tink.api.headers.TinkHttpHeaders;
import se.tink.libraries.metrics.Counter;
import se.tink.libraries.metrics.MeterFactory;
import se.tink.libraries.metrics.MetricId;

@Path("/api/v1/providers")
@Api(value = "Provider Service", description = "A service that can be used to list Providers.")
@Produces({
        MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF
})
public class ProviderServiceJerseyTransport implements ProviderService {
    private final ProviderServiceController providerServiceController;
    private final LoadingCache<MetricId.MetricLabels, Counter> listProviderOauthMeterCache;

    @Inject
    public ProviderServiceJerseyTransport(ProviderServiceController providerServiceController, MeterFactory meterFactory) {
        this.providerServiceController = providerServiceController;
        this.listProviderOauthMeterCache = meterFactory
                .createLoadingCache(MetricId.newId("oauth_requests"));
    }

    /**
     * This method is not bound on the HTTP layer with @GET since Jersey doesn't work with @QueryParam as the only
     * argument differing from another method. For HTTP layer the list(user, capability) method will be called as
     * list(user, null). Though we still need to provide a valid implementation of this method since we have in memory
     * service discovery that currently uses this implementation of ProviderService.
     */
    @Override
    public ProviderListResponse list(AuthenticatedUser user) {
        return list(user, null);
    }

    @GET
    @TeamOwnership(Team.INTEGRATION)
    @Override
    public ProviderListResponse list(
            @Authenticated(scopes = OAuth2AuthorizationScopeTypes.CREDENTIALS_READ) AuthenticatedUser user,
            @QueryParam("capability") Provider.Capability capability) {
        String userId = user.getUser().getId();
        String userMarket = user.getUser().getProfile().getMarket();

        List<Provider> providers = capability != null ?
                providerServiceController.list(userId, userMarket, capability) :
                providerServiceController.list(userId, userMarket);

        return new ProviderListResponse(providers);
    }

    @GET
    @Path("{market}/device/{deviceToken}")
    @TeamOwnership(Team.INTEGRATION)
    @ApiOperation(value = "List providers by device configuration",
            notes = "List all providers by given device configuration (by device available markets and feature flags). Using for the onboarding. This request doesn't require an authenticated user",
            response = ProviderListResponse.class
    )
    @Override
    public ProviderListResponse list(
            @ApiParam(value = "The (persistent) deviceToken", required = true) @PathParam("deviceToken") String deviceToken,
            @ApiParam(required = true, name = "market", value = "SE for Swedish market") @PathParam("market") String market) {
        try {
            return new ProviderListResponse(providerServiceController.list(UUID.fromString(deviceToken), market));
        } catch (NoSuchElementException e) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
    }

    @GET
    @Path("{market}")
    @TeamOwnership(Team.INTEGRATION)
    @ApiOperation(value = "List providers by Market",
            notes = "List all providers on given Market. Since this is an endpoint that doesn't require an authenticated user, API consumers should send their OAuth clientId as a header X-Tink-OAuth-Client-ID",
            response = ProviderListResponse.class
    )
    @Override
    public ProviderListResponse listByMarket(
            @Authenticated(required = false) AuthenticationContext authenticationContext,
            @ApiParam(required = true,
                    name = TinkHttpHeaders.OAUTH_CLIENT_ID_HEADER_NAME, value = "The OAuth2 Client ID") @HeaderParam(value = TinkHttpHeaders.OAUTH_CLIENT_ID_HEADER_NAME) String oauth2ClientIdHeader,
            @ApiParam(required = true,
                    name = "market", value = "SE for Swedish market") @PathParam("market") String market) {
        Optional<OAuth2Client> oAuth2Client = authenticationContext.getOAuth2Client();
        mark(oAuth2Client);

        return new ProviderListResponse(providerServiceController
                .listByMarket(OAuth2Utils.getPayloadValue(oAuth2Client, OAuth2Client.PayloadKey.PROVIDERS), market));
    }

    @GET
    @Path("suggest")
    @TeamOwnership(Team.INTEGRATION)
    @ApiOperation(value = "Suggest providers for user",
            response = ProviderListResponse.class
    )
    @Override
    public ProviderListResponse suggest(@Authenticated AuthenticatedUser authenticatedUser) {
        return new ProviderListResponse(
                Lists.newArrayList(providerServiceController.suggest(authenticatedUser.getUser())));
    }

    private void mark(Optional<OAuth2Client> oAuth2Client) {
        if (oAuth2Client.isPresent()) {
            // Mark meter with oauth2 client name
            String name = oAuth2Client.get().getName().toLowerCase();
            listProviderOauthMeterCache.getUnchecked(new MetricId.MetricLabels().add("client", name)).inc();
        }
    }
}
