package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.authenticator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.BawagPskApiClient;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.Envelope;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.LoginResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class BawagPskPasswordAuthenticator implements PasswordAuthenticator {

    private final BawagPskApiClient apiClient;
    private static final Logger logger = LoggerFactory.getLogger(BawagPskApiClient.class);

    public BawagPskPasswordAuthenticator(BawagPskApiClient client) {
        this.apiClient = client;
    }

    @Override
    public void authenticate(final String username, final String password)
            throws AuthenticationException, AuthorizationException {
        final String bankName = apiClient.getBankName();
        final LoginRequest request = new LoginRequest(username, password, bankName);
        final String requestBody;
        requestBody = request.getXml();

        try {
            final LoginResponse response = apiClient.login(requestBody);
            if (response.accountIsLocked()) {
                throw AuthorizationError.ACCOUNT_BLOCKED.exception();
            }
            if (response.incorrectCredentials()) {
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            }
        } catch (HttpResponseException e) {
            final Envelope errorResponse = e.getResponse().getBody(Envelope.class);
            if (errorResponse.credentialsAreIncorrect()) {
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            } else {
                // Unknown reason
                final String message = String.format("Failed to login because: %s",
                        errorResponse.getErrorMessage().orElse("Unknown reason"));
                throw new IllegalStateException(message);
            }
        }
    }
}
