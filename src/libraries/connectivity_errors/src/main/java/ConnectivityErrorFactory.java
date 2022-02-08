package src.libraries.connectivity_errors;

import se.tink.connectivity.errors.ConnectivityError;
import se.tink.connectivity.errors.ConnectivityErrorDetails;
import se.tink.connectivity.errors.ConnectivityErrorType;

public class ConnectivityErrorFactory {
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

    public static ConnectivityError fromLegacy(Exception exception) {
        return LegacyExceptionToConnectivityErrorMapper.from(exception);
    }
}
