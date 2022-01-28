package se.tink.backend.aggregation.agents.exceptions;

import se.tink.backend.aggregation.agents.exceptions.connectivity.ConnectivityException;
import se.tink.connectivity.errors.ConnectivityErrorDetails;
import se.tink.connectivity.errors.ConnectivityErrorType;

public class ConnectivityExceptionBusinessTypeResolver {

    public static boolean isSessionExpired(ConnectivityException ex) {
        return ConnectivityErrorType.AUTHORIZATION_ERROR.equals(ex.getError().getType())
                && ConnectivityErrorDetails.AuthorizationErrors.SESSION_EXPIRED
                        .name()
                        .equals(ex.getError().getDetails().getReason());
    }

    public static boolean isProviderError(ConnectivityException ex) {
        return ConnectivityErrorType.PROVIDER_ERROR.equals(ex.getError().getType());
    }

    public static boolean isDynamicCredentialsFlowTimeout(ConnectivityException ex) {
        return ConnectivityErrorType.USER_LOGIN_ERROR.equals(ex.getError().getType())
                && ConnectivityErrorDetails.UserLoginErrors.DYNAMIC_CREDENTIALS_FLOW_TIMEOUT
                        .name()
                        .equals(ex.getError().getDetails().getReason());
    }
}
