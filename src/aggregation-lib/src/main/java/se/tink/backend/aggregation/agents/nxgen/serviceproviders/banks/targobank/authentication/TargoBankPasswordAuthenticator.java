package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.authentication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.TargoBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.authentication.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.utils.TargoBankErrorCodes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.utils.TargoBankUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;

public class TargoBankPasswordAuthenticator implements PasswordAuthenticator {

    private final Logger LOGGER = LoggerFactory.getLogger(TargoBankPasswordAuthenticator.class);
    private final TargoBankApiClient apiClient;

    private TargoBankPasswordAuthenticator(TargoBankApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public static TargoBankPasswordAuthenticator create(TargoBankApiClient apiClient) {
        return new TargoBankPasswordAuthenticator(apiClient);
    }

    @Override
    public void authenticate(String username, String password) throws AuthenticationException, AuthorizationException {
        LoginResponse logon = apiClient.logon(username, password);
        if (!TargoBankUtils.isSuccess(logon.getReturnCode())) {
            TargoBankErrorCodes errorCode = TargoBankErrorCodes.getByCodeNumber(logon.getReturnCode());
            switch (errorCode) {
            case NOT_LOGGED_IN:
                throw SessionError.SESSION_EXPIRED.exception();
            case LOGIN_ERROR:
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            case TECHNICAL_PROBLEM:
                throw new IllegalStateException(TargoBankErrorCodes.TECHNICAL_PROBLEM.getCodeNumber());
            case NO_ENUM_VALUE:
                LOGGER.error("Unknown error code:" + logon);
                throw new UnknownError(TargoBankErrorCodes.NO_ENUM_VALUE + logon.getReturnCode());
            }
        }
    }

}
