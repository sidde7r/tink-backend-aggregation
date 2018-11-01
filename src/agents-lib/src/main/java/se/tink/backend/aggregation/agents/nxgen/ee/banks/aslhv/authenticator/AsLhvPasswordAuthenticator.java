package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.authenticator;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.AsLhvApiClient;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.AsLhvSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.rpc.GetCurrenciesResponse;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.rpc.IsAuthenticatedResponse;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.rpc.LoginResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class AsLhvPasswordAuthenticator implements PasswordAuthenticator {

    private final AsLhvApiClient apiClient;
    private final AsLhvSessionStorage sessionStorage;

    public AsLhvPasswordAuthenticator(final AsLhvApiClient client, final AsLhvSessionStorage sessionStorage) {
        this.apiClient = client;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void authenticate(final String username, final String password)
            throws AuthenticationException, AuthorizationException {

        try {
            LoginResponse loginResponse = apiClient.login(username, password);
            if (!loginResponse.isAuthenticated()) {
                if (loginResponse.incorrectCredentials()) {
                    throw LoginError.INCORRECT_CREDENTIALS.exception();
                }
            } else {
                GetCurrenciesResponse getCurrenciesResponse = apiClient.getCurrencies();
                if (getCurrenciesResponse.requestFailed()) {
                    final String errorMessage = String.format(
                            "Failed to fetch currencies: %s",
                            getCurrenciesResponse.getErrorMessage());
                    throw new IllegalStateException(errorMessage);
                }

                IsAuthenticatedResponse isAuthenticatedResponse = apiClient.isAuthenticated();
                if (isAuthenticatedResponse.requestFailed()) {
                    final String errorMessage = String.format(
                            "Authentication information request failed: %s",
                            isAuthenticatedResponse.getErrorMessage());
                    throw new IllegalStateException(errorMessage);
                }

                sessionStorage.setCurrencies(getCurrenciesResponse);
                sessionStorage.setIsAuthenticatedResponseData(isAuthenticatedResponse);
            }
        } catch (HttpResponseException e) {
            throw new IllegalStateException("Http request failed: " + e);
        } catch (HttpClientException e) {
            throw new IllegalStateException("Http client failed: " + e);
        }
    }
}
