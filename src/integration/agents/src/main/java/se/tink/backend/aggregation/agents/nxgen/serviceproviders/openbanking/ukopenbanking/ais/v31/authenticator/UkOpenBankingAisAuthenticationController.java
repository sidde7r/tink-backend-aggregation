package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.authenticator;

import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.consent.ConsentStatusValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticationValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.controller.OpenIdAuthenticationController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.entities.ClientMode;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.rpc.OpenIdErrorResponse;
import se.tink.backend.aggregation.logmasker.LogMasker;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.retrypolicy.RetryExecutor;
import se.tink.libraries.retrypolicy.RetryPolicy;

@Slf4j
public class UkOpenBankingAisAuthenticationController extends OpenIdAuthenticationController {

    private final UkOpenBankingApiClient apiClient;
    private final RetryExecutor retryExecutor = new RetryExecutor();
    private final ConsentStatusValidator consentStatusValidator;

    public UkOpenBankingAisAuthenticationController(
            PersistentStorage persistentStorage,
            SupplementalInformationHelper supplementalInformationHelper,
            UkOpenBankingApiClient apiClient,
            OpenIdAuthenticator authenticator,
            Credentials credentials,
            StrongAuthenticationState strongAuthenticationState,
            String callbackUri,
            RandomValueGenerator randomValueGenerator,
            OpenIdAuthenticationValidator authenticationValidator,
            ConsentStatusValidator consentStatusValidator,
            LogMasker logMasker) {
        super(
                persistentStorage,
                supplementalInformationHelper,
                apiClient,
                authenticator,
                credentials,
                strongAuthenticationState,
                callbackUri,
                randomValueGenerator,
                authenticationValidator,
                logMasker);

        this.apiClient = apiClient;
        this.retryExecutor.setRetryPolicy(new RetryPolicy(2, RuntimeException.class));
        this.consentStatusValidator = consentStatusValidator;
    }

    @Override
    public void autoAuthenticate() throws SessionException, BankServiceException {
        OAuth2Token clientOAuth2Token = requestClientToken();
        if (!clientOAuth2Token.isValid()) {
            throw new IllegalArgumentException("Client access token is not valid.");
        }

        apiClient.instantiateAisAuthFilter(clientOAuth2Token);

        consentStatusValidator.validate();
        super.autoAuthenticate();
    }

    // Temporary duplication until UkOpenBankingAisAuthenticationController is killed (IFD-3450)
    private OAuth2Token requestClientToken() {
        OAuth2Token clientToken;
        try {
            clientToken =
                    retryExecutor.execute(
                            () -> apiClient.requestClientCredentials(ClientMode.ACCOUNTS));
        } catch (HttpResponseException e) {
            HttpResponse response = e.getResponse();
            if (response.getStatus() >= 500) {
                log.warn(
                        "[OpenIdClientTokenRequester] Bank side error (status code {}) when "
                                + "requesting client token",
                        e.getResponse().getStatus());
                throw BankServiceError.BANK_SIDE_FAILURE.exception();
            }
            if (response.getStatus() == 403 && isAccessDenied(response)) {
                throwSessionExpiredException(e);
            }
            if (response.getStatus() == 400
                    && response.getBody(OpenIdErrorResponse.class).hasError("server_error")) {
                throwSessionExpiredException(e);
            }
            log.error(
                    "[OpenIdClientTokenRequester] Client token request failed: {}",
                    e.getResponse().getBody(String.class));
            throw SessionError.SESSION_EXPIRED.exception();

        } catch (HttpClientException e) {
            log.error(
                    "[OpenIdClientTokenRequester] Failure of processing the HTTP request or response: {}",
                    e.getMessage());
            throw SessionError.SESSION_EXPIRED.exception();
        }
        return clientToken;
    }

    private void throwSessionExpiredException(HttpResponseException e) {
        log.warn(
                "[OpenIdClientTokenRequester] Client token request failed with status code: {} and body: {} ",
                e.getResponse().getStatus(),
                e.getResponse().getBody(OpenIdErrorResponse.class));
        throw SessionError.SESSION_EXPIRED.exception();
    }

    private boolean isAccessDenied(HttpResponse response) {
        OpenIdErrorResponse errorResponse = response.getBody(OpenIdErrorResponse.class);
        return errorResponse.hasError("access denied")
                && errorResponse.containsErrorDescription("access denied");
    }
}
