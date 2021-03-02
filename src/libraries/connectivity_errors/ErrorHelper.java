package src.libraries.connectivity_errors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.ThirdPartyAppException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.exceptions.errors.SupplementalInfoError;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.exceptions.nemid.NemIdError;
import se.tink.backend.aggregation.agents.exceptions.nemid.NemIdException;
import se.tink.connectivity.errors.ConnectivityError;
import se.tink.connectivity.errors.ConnectivityErrorType;

public class ErrorHelper {
    private static final Logger log = LoggerFactory.getLogger(ErrorHelper.class);

    public static ConnectivityError from(ConnectivityErrorType type) {
        return ConnectivityError.newBuilder().setType(type).build();
    }

    public static ConnectivityError from(Exception exception) {

        // This should probably be ConnectivityErrorType.TINK_INTERNAL_SERVER_ERROR
        ConnectivityErrorType type = ConnectivityErrorType.ERROR_UNKNOWN;

        if (exception instanceof BankIdException) {
            BankIdError error = ((BankIdException) exception).getError();
            type = ExceptionMappings.BANKID_ERROR_MAPPER.get(error);
        } else if (exception instanceof BankServiceException) {
            BankServiceError error = ((BankServiceException) exception).getError();
            type = ExceptionMappings.BANK_SERVICE_ERROR_MAPPER.get(error);
        } else if (exception instanceof LoginException) {
            LoginError error = ((LoginException) exception).getError();
            type = ExceptionMappings.LOGIN_ERROR_MAPPER.get(error);
        } else if (exception instanceof ThirdPartyAppException) {
            ThirdPartyAppError error = ((ThirdPartyAppException) exception).getError();
            type = ExceptionMappings.THIRD_PARTY_APP_ERROR_MAPPER.get(error);
        } else if (exception instanceof SessionException) {
            SessionError error = ((SessionException) exception).getError();
            type = ExceptionMappings.SESSION_ERROR_MAPPER.get(error);
        } else if (exception instanceof SupplementalInfoException) {
            SupplementalInfoError error = ((SupplementalInfoException) exception).getError();
            type = ExceptionMappings.SUPPLEMENTAL_INFORMATION_ERROR_MAPPER.get(error);
        } else if (exception instanceof NemIdException) {
            NemIdError error = ((NemIdException) exception).getError();
            type = ExceptionMappings.NEM_ID_ERROR_MAPPER.get(error);
        } else if (exception instanceof AuthorizationException) {
            AuthorizationError error = ((AuthorizationException) exception).getError();
            type = ExceptionMappings.AUTHORIZATION_ERROR_MAPPER.get(error);
        }

        if (exception instanceof IllegalStateException
                || exception instanceof NullPointerException) {
            type = ConnectivityErrorType.ERROR_TINK_INTERNAL_ERROR;
        }

        if (type == ConnectivityErrorType.ERROR_UNKNOWN) {
            log.warn(
                    "[Login Result debugging]: Exception ({}) did not map to any Connectivity Error",
                    exception.getClass().getSimpleName(),
                    exception);
        }

        return ConnectivityError.newBuilder().setType(type).build();
    }
}
