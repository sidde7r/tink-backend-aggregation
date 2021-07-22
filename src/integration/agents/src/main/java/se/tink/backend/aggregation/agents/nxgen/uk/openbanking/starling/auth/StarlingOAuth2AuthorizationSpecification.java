package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.auth;

import static se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.StarlingConstants.CLIENT_ID_PARAM_KEY;
import static se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.StarlingConstants.CLIENT_SECRET_PARAM_KEY;
import static se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.StarlingConstants.Url.AUTH_STARLING;
import static se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.StarlingConstants.Url.GET_ACCESS_TOKEN;

import com.google.common.collect.Sets;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.consent.generators.uk.starling.StarlingConsentGenerator;
import se.tink.backend.aggregation.agents.consent.generators.uk.starling.StarlingScope;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.secrets.StarlingSecrets;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.oauth2.EndpointSpecification;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.oauth2.OAuth2AuthorizationSpecification;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AllArgsConstructor
public class StarlingOAuth2AuthorizationSpecification implements OAuth2AuthorizationSpecification {

    private final StarlingSecrets secrets;
    private final String redirectUrl;
    private final AgentComponentProvider componentProvider;

    public StarlingOAuth2AuthorizationSpecification(
            AgentConfiguration<StarlingSecrets> configuration,
            AgentComponentProvider componentProvider) {
        this.secrets = configuration.getProviderSpecificConfiguration();
        this.redirectUrl = configuration.getRedirectUrl();
        this.componentProvider = componentProvider;
    }

    @Override
    public Set<String> getScopes() {
        return StarlingConsentGenerator.of(
                        componentProvider,
                        Sets.newHashSet(
                                StarlingScope.ACCOUNT_HOLDER_TYPE_READ,
                                StarlingScope.ACCOUNT_HOLDER_NAME_READ,
                                StarlingScope.ACCOUNT_IDENTIFIER_READ,
                                StarlingScope.ACCOUNT_READ,
                                StarlingScope.TRANSACTION_READ,
                                StarlingScope.BALANCE_READ))
                .generate();
    }

    @Override
    public String getClientId() {
        return secrets.getAisClientId();
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
        return EndpointSpecification.builder(GET_ACCESS_TOKEN.toUri())
                .clientSpecificParam(CLIENT_SECRET_PARAM_KEY, secrets.getAisClientSecret())
                .build();
    }

    @Override
    public EndpointSpecification getRefreshTokenEndpoint() {
        return EndpointSpecification.builder(GET_ACCESS_TOKEN.toUri())
                .clientSpecificParam(CLIENT_SECRET_PARAM_KEY, secrets.getAisClientSecret())
                .clientSpecificParam(CLIENT_ID_PARAM_KEY, secrets.getAisClientId())
                .build();
    }
}
