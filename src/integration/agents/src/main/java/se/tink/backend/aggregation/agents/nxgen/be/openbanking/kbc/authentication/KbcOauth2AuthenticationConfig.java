package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.agentplatform.AgentPlatformHttpClient;
import se.tink.backend.aggregation.agents.agentplatform.authentication.OAuth2AuthenticationConfig;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication.persistence.KbcPersistedDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.configuration.KbcConfiguration;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectAuthenticationInitialProcessStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectAuthenticationProcess;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectFetchTokenCall;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectPreparationRedirectUrlStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectRefreshTokenCall;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectUrlBuilder;

@AllArgsConstructor
public class KbcOauth2AuthenticationConfig extends OAuth2AuthenticationConfig {

    private KbcConfiguration kbcConfiguration;
    private URI redirectUrl;
    private AgentPlatformHttpClient httpClient;
    private ObjectMapper objectMapper;

    public RedirectAuthenticationProcess authenticationProcess() {
        return new KbcRedirectAuthenticationProcess(
                initialProcessStep(),
                accessTokenValidationStep(),
                refreshTokenStep(refreshTokenCall()),
                preparationRedirectUrlStep(redirectUrlBuilder()),
                fetchAuthenticationTokensStep(redirectFetchTokenCall()),
                kbcFetchConsentAuthenticationStep());
    }

    @Override
    public RedirectAuthenticationInitialProcessStep initialProcessStep() {
        return new KbcRedirectAuthenticationInitialProcessStep(
                agentRedirectTokensAuthenticationPersistedDataAccessorFactory());
    }

    @Override
    public RedirectPreparationRedirectUrlStep preparationRedirectUrlStep(
            RedirectUrlBuilder redirectUrlBuilder) {
        return new KbcRedirectPreparationRedirectUrlStep(
                redirectUrlBuilder, kbcPersistedDataAccessorFactory());
    }

    public KbcPersistedDataAccessorFactory kbcPersistedDataAccessorFactory() {
        return new KbcPersistedDataAccessorFactory(objectMapper);
    }

    public RedirectUrlBuilder redirectUrlBuilder() {
        return new KbcRedirectUrlBuilder(
                kbcConfiguration.getClientId(), redirectUrl, kbcPersistedDataAccessorFactory());
    }

    public RedirectFetchTokenCall redirectFetchTokenCall() {
        return new KbcFetchTokenCall(
                httpClient, kbcConfiguration, redirectUrl, kbcPersistedDataAccessorFactory());
    }

    public RedirectRefreshTokenCall refreshTokenCall() {
        return new KbcRefreshTokenCall(httpClient, kbcConfiguration);
    }

    public KbcFetchConsentAuthenticationStep kbcFetchConsentAuthenticationStep() {
        return new KbcFetchConsentAuthenticationStep(
                redirectUrl,
                kbcConfiguration.getPsuIpAddress(),
                kbcFetchConsentExternalApiCall(),
                kbcPersistedDataAccessorFactory());
    }

    public KbcFetchConsentExternalApiCall kbcFetchConsentExternalApiCall() {
        return new KbcFetchConsentExternalApiCall(httpClient, kbcConfiguration.getBaseUrl());
    }
}
