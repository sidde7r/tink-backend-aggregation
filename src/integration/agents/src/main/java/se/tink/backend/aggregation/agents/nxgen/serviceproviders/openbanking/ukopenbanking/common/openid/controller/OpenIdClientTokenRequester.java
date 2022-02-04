package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.controller;

import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticationValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdConstants.Token;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.entities.ClientMode;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.rpc.OpenIdErrorResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.retrypolicy.RetryExecutor;
import se.tink.libraries.retrypolicy.RetryPolicy;

@Slf4j
public class OpenIdClientTokenRequester implements ClientTokenRequester {

    private final OpenIdApiClient apiClient;
    private final OpenIdAuthenticationValidator authenticationValidator;
    private final RetryExecutor retryExecutor;

    public OpenIdClientTokenRequester(
            OpenIdApiClient apiClient, OpenIdAuthenticationValidator authenticationValidator) {
        this.apiClient = apiClient;
        this.authenticationValidator = authenticationValidator;
        this.retryExecutor = new RetryExecutor();
        this.retryExecutor.setRetryPolicy(new RetryPolicy(2, RuntimeException.class));
    }

    @Override
    public OAuth2Token requestClientToken() {
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

        authenticationValidator.validateToken(clientToken, Token.CLIENT_ACCESS_TOKEN_MSG);
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
