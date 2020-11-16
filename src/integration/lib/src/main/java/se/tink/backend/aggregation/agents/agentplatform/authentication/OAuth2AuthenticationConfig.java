package se.tink.backend.aggregation.agents.agentplatform.authentication;

import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.AgentRedirectTokensAuthenticationPersistedDataAccessorFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectAuthenticationAccessTokenValidationStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectAuthenticationInitialProcessStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectAuthenticationProcess;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectAuthenticationRefreshTokenStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectFetchAuthenticationTokensStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectFetchTokenCall;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectPreparationRedirectUrlStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectRefreshTokenCall;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectTokensValidator;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectUrlBuilder;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.oauth2.DefaultOAuth2RedirectFetchTokenCall;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.oauth2.DefaultOAuth2RedirectUrlBuilder;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.oauth2.DefaultOAuth2RefreshTokenCall;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.oauth2.OAuth2AuthorizationSpecification;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.oauth2.OAuth2ErrorVerifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.AgentHttpClient;

public class OAuth2AuthenticationConfig {

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

    public RedirectAuthenticationProcess authenticationProcess(
            RedirectUrlBuilder redirectUrlBuilder,
            RedirectFetchTokenCall redirectFetchTokenCall,
            RedirectRefreshTokenCall refreshTokenCall) {
        return new RedirectAuthenticationProcess(
                initialProcessStep(),
                accessTokenValidationStep(),
                refreshTokenStep(refreshTokenCall),
                preparationRedirectUrlStep(redirectUrlBuilder),
                fetchAuthenticationTokensStep(redirectFetchTokenCall));
    }

    public RedirectAuthenticationInitialProcessStep initialProcessStep() {
        return new RedirectAuthenticationInitialProcessStep(
                agentRedirectTokensAuthenticationPersistedDataAccessorFactory());
    }

    public RedirectAuthenticationAccessTokenValidationStep accessTokenValidationStep() {
        return new RedirectAuthenticationAccessTokenValidationStep(
                agentRedirectTokensAuthenticationPersistedDataAccessorFactory(),
                redirectTokensValidator());
    }

    public RedirectAuthenticationRefreshTokenStep refreshTokenStep(
            RedirectRefreshTokenCall refreshTokenCall) {
        return new RedirectAuthenticationRefreshTokenStep(
                refreshTokenCall,
                agentRedirectTokensAuthenticationPersistedDataAccessorFactory(),
                redirectTokensValidator());
    }

    public RedirectPreparationRedirectUrlStep preparationRedirectUrlStep(
            RedirectUrlBuilder redirectUrlBuilder) {
        return new RedirectPreparationRedirectUrlStep(redirectUrlBuilder);
    }

    public RedirectFetchAuthenticationTokensStep fetchAuthenticationTokensStep(
            RedirectFetchTokenCall fetchTokenCall) {
        return new RedirectFetchAuthenticationTokensStep(
                fetchTokenCall,
                errorVerifier(),
                agentRedirectTokensAuthenticationPersistedDataAccessorFactory());
    }

    public OAuth2ErrorVerifier errorVerifier() {
        return new OAuth2ErrorVerifier();
    }

    public RedirectTokensValidator redirectTokensValidator() {
        return new RedirectTokensValidator();
    }

    public AgentRedirectTokensAuthenticationPersistedDataAccessorFactory
            agentRedirectTokensAuthenticationPersistedDataAccessorFactory() {
        return new AgentRedirectTokensAuthenticationPersistedDataAccessorFactory(
                new ObjectMapperFactory().getInstance());
    }
}
