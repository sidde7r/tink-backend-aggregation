package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.authenticator;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.AsLhvApiClient;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.rpc.LoginResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class AsLhvPasswordAuthenticator implements PasswordAuthenticator {

    private final AsLhvApiClient apiClient;
    private static final Logger logger = LoggerFactory.getLogger(AsLhvApiClient.class);

    public AsLhvPasswordAuthenticator(AsLhvApiClient client)
    {
        this.apiClient = client;
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
                apiClient.getCurrencies();
                apiClient.isAuthenticated();
            }
        } catch (HttpResponseException e) {
            e.printStackTrace();
            // TODO catch and throw proper exceptions.
        } catch (HttpClientException e) {
            e.printStackTrace();
            // TODO catch and throw proper exceptions.
        }
    }
}
