package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling;

import com.google.common.collect.ImmutableSet;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.configuration.entity.ClientConfigurationEntity;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.oauth2.EndpointSpecification;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.oauth2.OAuth2AuthorizationSpecification;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@AllArgsConstructor
public class StarlingOAuth2AuthorizationSpecification implements OAuth2AuthorizationSpecification {

    private static final String AUTH_STARLING = "https://oauth.starlingbank.com";
    private static final String API_STARLING = "https://api.starlingbank.com";
    private static final URL GET_OAUTH2_TOKEN_ENDPOINT =
            new URL(API_STARLING + "/oauth/access-token");
    private static final String CLIENT_ID_PARAM_KEY = "client_id";
    private static final String CLIENT_SECRET_PARAM_KEY = "client_secret";

    private final ClientConfigurationEntity aisConfiguration;
    private final String redirectUrl;

    @Override
    public Set<String> getScopes() {
        return ImmutableSet.of(
                "account-holder-type:read",
                "customer:read",
                "account-identifier:read",
                "account:read",
                "transaction:read",
                "balance:read");
    }

    @Override
    public String getClientId() {
        return aisConfiguration.getClientId();
    }

    @Override
    public URI getAuthorizationEndpoint() {
        try {
            return new URI(AUTH_STARLING);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public URI getRedirectUrl() {
        try {
            return new URI(redirectUrl);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public EndpointSpecification getAccessTokenEndpoint() {
        return EndpointSpecification.builder(GET_OAUTH2_TOKEN_ENDPOINT.toUri())
                .clientSpecificParam(CLIENT_SECRET_PARAM_KEY, aisConfiguration.getClientSecret())
                .build();
    }

    @Override
    public EndpointSpecification getRefreshTokenEndpoint() {
        return EndpointSpecification.builder(GET_OAUTH2_TOKEN_ENDPOINT.toUri())
                .clientSpecificParam(CLIENT_SECRET_PARAM_KEY, aisConfiguration.getClientSecret())
                .clientSpecificParam(CLIENT_ID_PARAM_KEY, aisConfiguration.getClientId())
                .build();
    }
}
