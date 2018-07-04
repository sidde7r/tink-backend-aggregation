package se.tink.backend.aggregation.agents.nxgen.at.banks.bawagpsk.authenticator;

import javax.xml.bind.JAXBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bawagpsk.BawagPskApiClient;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bawagpsk.entities.Envelope;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bawagpsk.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bawagpsk.rpc.LoginResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class BawagPskPasswordAuthenticator implements PasswordAuthenticator {

    private final BawagPskApiClient bawagPskApiClient;
    private static final Logger logger = LoggerFactory.getLogger(BawagPskApiClient.class);

    public BawagPskPasswordAuthenticator(BawagPskApiClient client) {
        this.bawagPskApiClient = client;
    }

    @Override
    public void authenticate(final String username, final String password)
            throws AuthenticationException, AuthorizationException {
        final LoginRequest request = new LoginRequest(username, password);
        final String requestBody;
        try {
            requestBody = request.getXml();
        } catch (JAXBException e) {
            throw new IllegalStateException("Unable to marshal JAXB ", e);
        }

        try {
            final LoginResponse response = bawagPskApiClient.login(requestBody);
            if (response.accountIsLocked()) {
                throw AuthorizationError.ACCOUNT_BLOCKED.exception();
            }
        } catch (HttpResponseException e) {
            final Envelope errorResponse = e.getResponse().getBody(Envelope.class);
            if (errorResponse.credentialsAreIncorrect()) {
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            } else {
                // Unknown reason
                final String message = String.format("Failed to login because: %s",
                        errorResponse.getErrorMessage().orElse("Unknown reason"));
                logger.error(message);
                throw new IllegalStateException(message);
            }
        }
    }
}
