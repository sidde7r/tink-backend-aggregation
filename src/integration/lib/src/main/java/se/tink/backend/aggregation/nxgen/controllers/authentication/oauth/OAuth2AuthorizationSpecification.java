package se.tink.backend.aggregation.nxgen.controllers.authentication.oauth;

import com.google.common.base.Preconditions;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class OAuth2AuthorizationSpecification {

    private static final String RESPONSE_TYPE_CODE = "code";
    private static final String RESPONSE_TYPE_TOKEN = "token";

    private String responseType;
    private String scope;
    private String clientId;
    private URL redirectUrl;
    private EndpointSpecification authorizationEndpoint;
    private EndpointSpecification refreshTokenEndpoint;
    private EndpointSpecification accessTokenEndpoint;
    private Long defaultAccessTokenLifetime;
    private Set<String> accessTokenResponseClientSpecificProperties = new HashSet<>();

    private OAuth2AuthorizationSpecification() {}

    public String getResponseType() {
        return responseType;
    }

    public Optional<String> getScope() {
        return Optional.ofNullable(scope);
    }

    public String getClientId() {
        return clientId;
    }

    public URL getRedirectUrl() {
        return redirectUrl;
    }

    public EndpointSpecification getAuthorizationEndpoint() {
        return authorizationEndpoint;
    }

    public EndpointSpecification getRefreshTokenEndpoint() {
        return Optional.ofNullable(refreshTokenEndpoint).orElse(authorizationEndpoint);
    }

    public boolean isResponseTypeCode() {
        return RESPONSE_TYPE_CODE.equals(responseType);
    }

    public EndpointSpecification getAccessTokenEndpoint() {
        return accessTokenEndpoint;
    }

    public Optional<Long> getDefaultAccessTokenLifetime() {
        return Optional.ofNullable(defaultAccessTokenLifetime);
    }

    public Set<String> getAccessTokenResponseClientSpecificProperties() {
        return accessTokenResponseClientSpecificProperties;
    }

    public static class Builder {

        private OAuth2AuthorizationSpecification endpointProvider;

        public Builder() {
            endpointProvider = new OAuth2AuthorizationSpecification();
        }

        public Builder withScopes(final String... scopes) {
            endpointProvider.scope = String.join(" ", scopes);
            return this;
        }

        public Builder withClientId(final String clientId) {
            endpointProvider.clientId = clientId;
            return this;
        }

        public Builder withRedirectUrl(final URL redirectUrl) {
            endpointProvider.redirectUrl = redirectUrl;
            return this;
        }

        public Builder withRedirectUrl(final String redirectUrl) {
            try {
                withRedirectUrl(new URL(redirectUrl));
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("Malformed URL");
            }
            return this;
        }

        public Builder withAuthorizationEndpoint(final EndpointSpecification endpoint) {
            endpointProvider.authorizationEndpoint = endpoint;
            return this;
        }

        public Builder withTokenRefreshEndpoint(final EndpointSpecification endpoint) {
            endpointProvider.refreshTokenEndpoint = endpoint;
            return this;
        }

        public Builder withResponseTypeCode(final EndpointSpecification tokenEndpoint) {
            Preconditions.checkNotNull(tokenEndpoint, "Access token endpoint is mandatory");
            endpointProvider.accessTokenEndpoint = tokenEndpoint;
            endpointProvider.responseType = RESPONSE_TYPE_CODE;
            return this;
        }

        public Builder withResponseTypeToken() {
            endpointProvider.responseType = RESPONSE_TYPE_TOKEN;
            return this;
        }

        public Builder withDefaultAccessTokenLifetime(final long miliseconds) {
            endpointProvider.defaultAccessTokenLifetime = miliseconds;
            return this;
        }

        public Builder withAccessTokenResponseClientSpecificProperty(final String propertyName) {
            endpointProvider.accessTokenResponseClientSpecificProperties.add(propertyName);
            return this;
        }

        public OAuth2AuthorizationSpecification build() {
            Preconditions.checkState(
                    endpointProvider.responseType != null,
                    "OAuth2 'response_type' param is mandatory");
            Preconditions.checkState(
                    endpointProvider.clientId != null, "OAuth2 'client_id' param is mandatory");
            Preconditions.checkState(
                    endpointProvider.redirectUrl != null,
                    "OAuth2 'redirect_uri; param is mandatory");
            Preconditions.checkState(
                    endpointProvider.authorizationEndpoint != null,
                    "OAuth2 authorization server URL is mandatory");
            return endpointProvider;
        }
    }
}
