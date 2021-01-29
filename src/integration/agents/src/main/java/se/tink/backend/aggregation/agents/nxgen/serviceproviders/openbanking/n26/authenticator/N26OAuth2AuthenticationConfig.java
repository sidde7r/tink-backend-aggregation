package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.agentplatform.AgentPlatformHttpClient;
import se.tink.backend.aggregation.agents.agentplatform.authentication.OAuth2AuthenticationConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.N26AgentConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.N26Constants.Url;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.authentication_init.N26AuthenticationInitStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.authorization_redirect.N26RedirectUrlBuilder;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.await_user_confirmation.N26AwaitUserConfirmationStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.fetch_authorization_url.N26FetchAuthorizationUrlApiCall;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.fetch_authorization_url.N26FetchAuthorizationUrlStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.fetch_consent.N26FetchConsentApiCall;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.fetch_consent.N26FetchConsentStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.fetch_token.N26FetchTokenParameters;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.fetch_token.N26OAuth2RedirectFetchTokenCall;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.fetch_token.N26RedirectFetchAuthenticationTokensStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.redirect_authentication.N26RedirectAuthenticationAccessTokenValidationStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.redirect_authentication.N26RedirectAuthenticationRefreshTokenStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.refresh_token.N26OAuth2RefreshTokenCall;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.refresh_token.N26RefreshTokenParameters;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.validate_consent.N26AutoAuthValidateConsentStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.validate_consent.N26ValidateConsentApiCall;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.validate_consent.N26ValidateConsentStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcess;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectAuthenticationAccessTokenValidationStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectAuthenticationInitialProcessStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectAuthenticationProcess;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectAuthenticationRefreshTokenStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectFetchRefreshableAccessTokenStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectFetchTokenCall;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectRefreshTokenCall;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectUrlBuilder;
import se.tink.libraries.i18n.Catalog;

@AllArgsConstructor
public class N26OAuth2AuthenticationConfig extends OAuth2AuthenticationConfig {

    private AgentPlatformHttpClient agentHttpClient;
    private ObjectMapper objectMapper;
    private N26AgentConfiguration n26AgentConfiguration;
    private Catalog catalog;

    public AgentAuthenticationProcess authenticationProcess() {
        return new RedirectAuthenticationProcess(
                initialProcessStep(),
                accessTokenValidationStep(),
                refreshTokenStep(refreshTokenCall()),
                preparationRedirectUrlStep(redirectUrlBuilder()),
                fetchAuthenticationTokensStep(redirectFetchTokenCall())) {
            @Override
            public void registerSteps() {
                super.registerSteps();
                addStep(getN26FetchAuthorizationUrlStep());
                addStep(getN26AutoAuthValidateConsentStep());
                addStep(getN26FetchConsentStep());
                addStep(getN26AwaitUserConfirmationStep());
                addStep(getN26ValidateConsentStep());
            }
        };
    }

    @Override
    public RedirectAuthenticationInitialProcessStep initialProcessStep() {
        return new N26AuthenticationInitStep(
                agentRedirectTokensAuthenticationPersistedDataAccessorFactory());
    }

    @Override
    public RedirectAuthenticationAccessTokenValidationStep accessTokenValidationStep() {
        return new N26RedirectAuthenticationAccessTokenValidationStep(
                agentRedirectTokensAuthenticationPersistedDataAccessorFactory(),
                redirectTokensValidator());
    }

    @Override
    public RedirectAuthenticationRefreshTokenStep refreshTokenStep(
            RedirectRefreshTokenCall refreshTokenCall) {
        return new N26RedirectAuthenticationRefreshTokenStep(
                refreshTokenCall,
                agentRedirectTokensAuthenticationPersistedDataAccessorFactory(),
                redirectTokensValidator());
    }

    @Override
    public RedirectFetchRefreshableAccessTokenStep fetchAuthenticationTokensStep(
            RedirectFetchTokenCall fetchTokenCall) {
        return new N26RedirectFetchAuthenticationTokensStep(
                fetchTokenCall,
                errorVerifier(),
                agentRedirectTokensAuthenticationPersistedDataAccessorFactory());
    }

    private N26FetchAuthorizationUrlStep getN26FetchAuthorizationUrlStep() {
        N26FetchAuthorizationUrlApiCall apiCall =
                new N26FetchAuthorizationUrlApiCall(agentHttpClient, Url.BASE_URL);
        return new N26FetchAuthorizationUrlStep(
                apiCall,
                n26AgentConfiguration.getClientId(),
                n26AgentConfiguration.getRedirectUrl(),
                getN26CryptoService(),
                objectMapper);
    }

    private N26AutoAuthValidateConsentStep getN26AutoAuthValidateConsentStep() {
        N26ValidateConsentApiCall apiCall =
                new N26ValidateConsentApiCall(agentHttpClient, Url.BASE_URL);
        return new N26AutoAuthValidateConsentStep(apiCall, objectMapper);
    }

    private N26FetchConsentStep getN26FetchConsentStep() {
        N26FetchConsentApiCall apiCall = new N26FetchConsentApiCall(agentHttpClient, Url.BASE_URL);
        return new N26FetchConsentStep(apiCall, objectMapper);
    }

    private N26AwaitUserConfirmationStep getN26AwaitUserConfirmationStep() {
        return new N26AwaitUserConfirmationStep(objectMapper, catalog);
    }

    private N26ValidateConsentStep getN26ValidateConsentStep() {
        N26ValidateConsentApiCall n26ValidateConsentApiCall =
                new N26ValidateConsentApiCall(agentHttpClient, Url.BASE_URL);
        return new N26ValidateConsentStep(n26ValidateConsentApiCall, objectMapper);
    }

    private RedirectUrlBuilder redirectUrlBuilder() {
        return new N26RedirectUrlBuilder(objectMapper);
    }

    private RedirectRefreshTokenCall refreshTokenCall() {
        return new N26OAuth2RefreshTokenCall(agentHttpClient, fetchRefreshTokenParameters());
    }

    private RedirectFetchTokenCall redirectFetchTokenCall() {
        return new N26OAuth2RedirectFetchTokenCall(
                agentHttpClient, fetchTokenParameters(), objectMapper);
    }

    private N26FetchTokenParameters fetchTokenParameters() {
        return N26FetchTokenParameters.builder()
                .baseUrl(Url.BASE_URL)
                .clientId(n26AgentConfiguration.getClientId())
                .redirectUrl(n26AgentConfiguration.getRedirectUrl())
                .build();
    }

    private N26RefreshTokenParameters fetchRefreshTokenParameters() {
        return N26RefreshTokenParameters.builder()
                .baseUrl(Url.BASE_URL)
                .clientId(n26AgentConfiguration.getClientId())
                .build();
    }

    private N26CryptoService getN26CryptoService() {
        return new N26CryptoService();
    }
}
