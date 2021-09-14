package se.tink.backend.aggregation.agents.exceptions.connectivity;

import static lombok.AccessLevel.PRIVATE;

import lombok.NoArgsConstructor;
import se.tink.connectivity.errors.ConnectivityError;
import se.tink.connectivity.errors.ConnectivityErrorDetails;
import se.tink.connectivity.errors.ConnectivityErrorType;
import se.tink.libraries.i18n.LocalizableKey;

@NoArgsConstructor(access = PRIVATE)
public final class ConnectivityExceptionFactory {

    private static final AccountInformationErrorDefaultMessageProvider
            ACCOUNT_INFORMATION_ERROR_DEFAULT_MESSAGE_PROVIDER =
                    new AccountInformationErrorDefaultMessageProvider();
    private static final AuthorizationErrorDefaultMessageProvider
            AUTHORIZATION_ERROR_DEFAULT_MESSAGE_PROVIDER =
                    new AuthorizationErrorDefaultMessageProvider();
    private static final ProviderErrorDefaultMessageProvider
            PROVIDER_ERROR_DEFAULT_MESSAGE_PROVIDER = new ProviderErrorDefaultMessageProvider();
    private static final TinkSideErrorDefaultMessageProvider
            TINK_SIDE_ERROR_DEFAULT_MESSAGE_PROVIDER = new TinkSideErrorDefaultMessageProvider();
    private static final UserLoginErrorDefaultMessageProvider
            USER_LOGIN_ERROR_DEFAULT_MESSAGE_PROVIDER = new UserLoginErrorDefaultMessageProvider();

    public static ConnectivityException createAccountInformationException(
            ConnectivityErrorDetails.AccountInformationErrors reason) {
        return createAccountInformationException(
                reason, ACCOUNT_INFORMATION_ERROR_DEFAULT_MESSAGE_PROVIDER.provide(reason));
    }

    public static ConnectivityException createAccountInformationException(
            ConnectivityErrorDetails.AccountInformationErrors reason, LocalizableKey userMessage) {
        return new ConnectivityException(
                buildConnectivityError(
                        ConnectivityErrorType.ACCOUNT_INFORMATION_ERROR,
                        reason.name(),
                        userMessage));
    }

    public static ConnectivityException createAuthorizationException(
            ConnectivityErrorDetails.AuthorizationErrors reason) {
        return createAuthorizationException(
                reason, AUTHORIZATION_ERROR_DEFAULT_MESSAGE_PROVIDER.provide(reason));
    }

    public static ConnectivityException createAuthorizationException(
            ConnectivityErrorDetails.AuthorizationErrors reason, LocalizableKey userMessage) {
        return new ConnectivityException(
                buildConnectivityError(
                        ConnectivityErrorType.AUTHORIZATION_ERROR, reason.name(), userMessage));
    }

    public static ConnectivityException createProviderException(
            ConnectivityErrorDetails.ProviderErrors reason) {
        return createProviderException(
                reason, PROVIDER_ERROR_DEFAULT_MESSAGE_PROVIDER.provide(reason));
    }

    public static ConnectivityException createProviderException(
            ConnectivityErrorDetails.ProviderErrors reason, LocalizableKey userMessage) {
        return new ConnectivityException(
                buildConnectivityError(
                        ConnectivityErrorType.PROVIDER_ERROR, reason.name(), userMessage));
    }

    public static ConnectivityException createTinkSideException(
            ConnectivityErrorDetails.TinkSideErrors reason) {
        return createTinkSideException(
                reason, TINK_SIDE_ERROR_DEFAULT_MESSAGE_PROVIDER.provide(reason));
    }

    public static ConnectivityException createTinkSideException(
            ConnectivityErrorDetails.TinkSideErrors reason, LocalizableKey userMessage) {
        return new ConnectivityException(
                buildConnectivityError(
                        ConnectivityErrorType.TINK_SIDE_ERROR, reason.name(), userMessage));
    }

    public static ConnectivityException createUserLoginException(
            ConnectivityErrorDetails.UserLoginErrors reason) {
        return createUserLoginException(
                reason, USER_LOGIN_ERROR_DEFAULT_MESSAGE_PROVIDER.provide(reason));
    }

    public static ConnectivityException createUserLoginException(
            ConnectivityErrorDetails.UserLoginErrors reason, LocalizableKey userMessage) {
        return new ConnectivityException(
                buildConnectivityError(
                        ConnectivityErrorType.USER_LOGIN_ERROR, reason.name(), userMessage));
    }

    private static ConnectivityError buildConnectivityError(
            ConnectivityErrorType authorizationError, String name, LocalizableKey userMessage) {
        return ConnectivityError.newBuilder()
                .setType(authorizationError)
                .setDetails(ConnectivityErrorDetails.newBuilder().setReason(name).build())
                .setDisplayMessage(userMessage.get())
                .build();
    }
}
