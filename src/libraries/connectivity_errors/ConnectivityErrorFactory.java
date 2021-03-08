package src.libraries.connectivity_errors;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;
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
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.connectivity.errors.ConnectivityError;
import se.tink.connectivity.errors.ConnectivityErrorDetails;
import se.tink.connectivity.errors.ConnectivityErrorType;

public class ConnectivityErrorFactory {
    private static final Logger log = LoggerFactory.getLogger(ConnectivityErrorFactory.class);

    public static ConnectivityError tinkSideError(ConnectivityErrorDetails.TinkSideErrors reason) {
        return ConnectivityError.newBuilder()
                .setType(ConnectivityErrorType.TINK_SIDE_ERROR)
                .setDetails(ConnectivityErrorDetails.newBuilder().setReason(reason.name()).build())
                .build();
    }

    public static ConnectivityError providerError(ConnectivityErrorDetails.ProviderErrors reason) {
        return ConnectivityError.newBuilder()
                .setType(ConnectivityErrorType.PROVIDER_ERROR)
                .setDetails(ConnectivityErrorDetails.newBuilder().setReason(reason.name()).build())
                .build();
    }

    public static ConnectivityError userLoginError(
            ConnectivityErrorDetails.UserLoginErrors reason) {
        return ConnectivityError.newBuilder()
                .setType(ConnectivityErrorType.USER_LOGIN_ERROR)
                .setDetails(ConnectivityErrorDetails.newBuilder().setReason(reason.name()).build())
                .build();
    }

    public static ConnectivityError authorizationError(
            ConnectivityErrorDetails.AuthorizationErrors reason) {
        return ConnectivityError.newBuilder()
                .setType(ConnectivityErrorType.AUTHORIZATION_ERROR)
                .setDetails(ConnectivityErrorDetails.newBuilder().setReason(reason.name()).build())
                .build();
    }

    public static ConnectivityError accountInformationError(
            ConnectivityErrorDetails.AccountInformationErrors reason) {
        return ConnectivityError.newBuilder()
                .setType(ConnectivityErrorType.ACCOUNT_INFORMATION_ERROR)
                .setDetails(ConnectivityErrorDetails.newBuilder().setReason(reason.name()).build())
                .build();
    }

    public static ConnectivityError from(Exception exception) {

        // This should probably be ConnectivityErrorType.TINK_INTERNAL_SERVER_ERROR
        ConnectivityErrorType type = ConnectivityErrorType.UNKNOWN_ERROR;

        /* Inherited from AgentException */
        if (exception instanceof BankIdException) {
            BankIdError error = ((BankIdException) exception).getError();
            type = LegacyExceptionToConnectivityErrorMapper.BANKID_ERROR_MAPPER.get(error);
        } else if (exception instanceof BankServiceException) {
            BankServiceError error = ((BankServiceException) exception).getError();
            type = LegacyExceptionToConnectivityErrorMapper.BANK_SERVICE_ERROR_MAPPER.get(error);
        } else if (exception instanceof LoginException) {
            LoginError error = ((LoginException) exception).getError();
            type = LegacyExceptionToConnectivityErrorMapper.LOGIN_ERROR_MAPPER.get(error);
        } else if (exception instanceof ThirdPartyAppException) {
            ThirdPartyAppError error = ((ThirdPartyAppException) exception).getError();
            type = LegacyExceptionToConnectivityErrorMapper.THIRD_PARTY_APP_ERROR_MAPPER.get(error);
        } else if (exception instanceof SessionException) {
            SessionError error = ((SessionException) exception).getError();
            type = LegacyExceptionToConnectivityErrorMapper.SESSION_ERROR_MAPPER.get(error);
        } else if (exception instanceof SupplementalInfoException) {
            SupplementalInfoError error = ((SupplementalInfoException) exception).getError();
            type =
                    LegacyExceptionToConnectivityErrorMapper.SUPPLEMENTAL_INFORMATION_ERROR_MAPPER
                            .get(error);
        } else if (exception instanceof NemIdException) {
            NemIdError error = ((NemIdException) exception).getError();
            type = LegacyExceptionToConnectivityErrorMapper.NEM_ID_ERROR_MAPPER.get(error);
        } else if (exception instanceof AuthorizationException) {
            AuthorizationError error = ((AuthorizationException) exception).getError();
            type = LegacyExceptionToConnectivityErrorMapper.AUTHORIZATION_ERROR_MAPPER.get(error);
        }

        /* Http Client Exceptions */
        if (exception instanceof HttpClientException
                || exception instanceof HttpResponseException
                || exception instanceof ClientHandlerException
                || exception instanceof UniformInterfaceException) {
            log.warn(
                    "Unhandled Http Client exceptions ({}) in agent. This should be fixed.",
                    exception.getClass().getSimpleName(),
                    exception);
            type = ConnectivityErrorType.ERROR_TINK_INTERNAL_ERROR;
        }

        /* Java lang exceptions */
        if (exception instanceof NullPointerException
                || exception instanceof IndexOutOfBoundsException
                || exception instanceof NumberFormatException
                || exception instanceof NotImplementedException) {
            log.error(
                    "Bug in agent code that throws {}. This should be fixed.",
                    exception.getClass().getSimpleName(),
                    exception);
            type = ConnectivityErrorType.ERROR_TINK_INTERNAL_ERROR;
        } else if (exception instanceof IllegalStateException) {
            type = ConnectivityErrorType.ERROR_TINK_INTERNAL_ERROR;
        }

        if (type == ConnectivityErrorType.UNKNOWN_ERROR) {
            log.warn(
                    "[Login Result debugging]: Exception ({}) did not map to any Connectivity Error",
                    exception.getClass().getSimpleName(),
                    exception);
        }

        return ConnectivityError.newBuilder().setType(type).build();
    }
}
