package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling;

import static se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.StarlingConstants.CLIENT_ID_PARAM_KEY;
import static se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.StarlingConstants.CLIENT_SECRET_PARAM_KEY;
import static se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.StarlingConstants.Url.AUTH_STARLING;
import static se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.StarlingConstants.Url.GET_ACCESS_TOKEN;

import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.configuration.entity.ClientConfigurationEntity;
import se.tink.backend.aggregation.nxgen.controllers.authentication.oauth.EndpointSpecification;
import se.tink.backend.aggregation.nxgen.controllers.authentication.oauth.OAuth2AuthorizationSpecification;
import se.tink.backend.aggregation.nxgen.controllers.authentication.oauth.progressive.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class StarlingOAut2Authenticator extends OAuth2Authenticator {

    private static final String[] SCOPES =
            new String[] {
                "account-holder-type:read",
                "customer:read",
                "account-identifier:read",
                "account:read",
                "transaction:read",
                "balance:read"
            };

    public StarlingOAut2Authenticator(
            final PersistentStorage persistentStorage,
            final TinkHttpClient httpClient,
            final ClientConfigurationEntity aisConfiguration,
            final String redirectUrl,
            final StrongAuthenticationState strongAuthenticationState) {
        super(
                createAuthorizationSpecification(aisConfiguration, redirectUrl),
                persistentStorage,
                httpClient,
                strongAuthenticationState);
    }

    private static OAuth2AuthorizationSpecification createAuthorizationSpecification(
            final ClientConfigurationEntity aisConfiguration, final String redirectUrl) {
        return new OAuth2AuthorizationSpecification.Builder()
                .withAuthorizationEndpoint(new EndpointSpecification(AUTH_STARLING))
                .withRedirectUrl(redirectUrl)
                .withClientId(aisConfiguration.getClientId())
                .withResponseTypeCode(createAccessTokenEndpoint(aisConfiguration))
                .withTokenRefreshEndpoint(createRefreshTokenEndpointSpecification(aisConfiguration))
                .withScopes(SCOPES)
                .build();
    }

    private static EndpointSpecification createRefreshTokenEndpointSpecification(
            final ClientConfigurationEntity aisConfiguration) {
        return new EndpointSpecification(GET_ACCESS_TOKEN.toString())
                .withClientSpecificParameter(CLIENT_ID_PARAM_KEY, aisConfiguration.getClientId())
                .withClientSpecificParameter(
                        CLIENT_SECRET_PARAM_KEY, aisConfiguration.getClientSecret());
    }

    private static EndpointSpecification createAccessTokenEndpoint(
            final ClientConfigurationEntity aisConfiguration) {
        return new EndpointSpecification(GET_ACCESS_TOKEN.toString())
                .withClientSpecificParameter(
                        CLIENT_SECRET_PARAM_KEY, aisConfiguration.getClientSecret());
    }
}
