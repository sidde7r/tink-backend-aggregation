package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.authenticator;

import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Predicates;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;

public class FinTsAuthenticator implements PasswordAuthenticator {

    private FinTsApiClient apiClient;
    private static final Logger LOGGER = LoggerFactory.getLogger(FinTsAuthenticator.class);

    public FinTsAuthenticator(FinTsApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public void authenticate(String username, String password)
            throws AuthenticationException, AuthorizationException {
        Collection<String> status = apiClient.sync();

        if (!status.isEmpty()) {
            // Different banks use different status code for invalid password, makes it difficult to filter the error cause.
            if (status.contains(FinTsConstants.StatusCode.PIN_TEMP_BLOCKED) ||
                    status.contains(FinTsConstants.StatusCode.ACTION_LOCKED)) {
                throw AuthorizationError.ACCOUNT_BLOCKED.exception();
            } else if (status.contains(FinTsConstants.StatusCode.INVALID_USER) ||
                    status.contains(FinTsConstants.StatusCode.INVALID_PIN) ||
                    status.contains(FinTsConstants.StatusCode.LOGIN_FAILED)) {
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            } else {
                LOGGER.warn(FinTsConstants.LogTags.ERROR_CODE.toString(), status);
            }
        }

        apiClient.init();
    }
}
