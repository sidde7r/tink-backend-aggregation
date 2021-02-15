package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.auth;

import se.tink.backend.aggregation.agents.agentplatform.authentication.OAuth2AuthenticationConfig;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.auth.steps.StarlingRefreshTokenStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectAuthenticationProcess;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectAuthenticationRefreshTokenStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectRefreshTokenCall;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.oauth2.DefaultOAuth2RedirectFetchTokenCall;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.oauth2.DefaultOAuth2RedirectUrlBuilder;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.oauth2.DefaultOAuth2RefreshTokenCall;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.oauth2.OAuth2AuthorizationSpecification;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.AgentHttpClient;

public class StarlingOAuth2AuthenticationConfig extends OAuth2AuthenticationConfig {

    @Override
    public RedirectAuthenticationProcess authenticationProcess(
            AgentHttpClient httpClient,
            OAuth2AuthorizationSpecification oAuth2AuthorizationSpecification) {

        return new RedirectAuthenticationProcess(
                initialProcessStep(),
                accessTokenValidationStep(),
                refreshTokenStep(
                        new DefaultOAuth2RefreshTokenCall(
                                httpClient, oAuth2AuthorizationSpecification)),
                preparationRedirectUrlStep(
                        new DefaultOAuth2RedirectUrlBuilder(oAuth2AuthorizationSpecification)),
                fetchAuthenticationTokensStep(
                        new DefaultOAuth2RedirectFetchTokenCall(
                                httpClient, oAuth2AuthorizationSpecification)));
    }

    public RedirectAuthenticationRefreshTokenStep refreshTokenStep(
            RedirectRefreshTokenCall refreshTokenCall) {
        return new StarlingRefreshTokenStep(
                refreshTokenCall,
                agentRedirectTokensAuthenticationPersistedDataAccessorFactory(),
                redirectTokensValidator());
    }
}
