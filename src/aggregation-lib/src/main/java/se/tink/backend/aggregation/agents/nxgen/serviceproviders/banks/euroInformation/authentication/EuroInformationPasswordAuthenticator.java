package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroInformation.authentication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroInformation.EuroInformationApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroInformation.authentication.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroInformation.utils.EuroInformationErrorCodes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroInformation.utils.EuroInformationUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;

public class EuroInformationPasswordAuthenticator implements PasswordAuthenticator {

    private final Logger LOGGER = LoggerFactory.getLogger(EuroInformationPasswordAuthenticator.class);
    private final EuroInformationApiClient apiClient;

    private EuroInformationPasswordAuthenticator(EuroInformationApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public static EuroInformationPasswordAuthenticator create(EuroInformationApiClient apiClient) {
        return new EuroInformationPasswordAuthenticator(apiClient);
    }

    @Override
    public void authenticate(String username, String password) throws AuthenticationException, AuthorizationException {
        LoginResponse logon = apiClient.logon(username, password);
        if (!EuroInformationUtils.isSuccess(logon.getReturnCode())) {
            EuroInformationErrorCodes errorCode = EuroInformationErrorCodes.getByCodeNumber(logon.getReturnCode());
            switch (errorCode) {
            case NOT_LOGGED_IN:
                throw SessionError.SESSION_EXPIRED.exception();
            case LOGIN_ERROR:
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            case TECHNICAL_PROBLEM:
                throw new IllegalStateException(EuroInformationErrorCodes.TECHNICAL_PROBLEM.getCodeNumber());
            case NO_ENUM_VALUE:
                LOGGER.error("Unknown error code:" + logon);
                throw new UnknownError(EuroInformationErrorCodes.NO_ENUM_VALUE + logon.getReturnCode());
            }
        }
    }

}
