package se.tink.backend.aggregation.agents.exceptions.connectivity;

import lombok.Getter;
import lombok.NonNull;
import lombok.With;
import se.tink.connectivity.errors.ConnectivityError;
import se.tink.connectivity.errors.ConnectivityErrorDetails;
import se.tink.connectivity.errors.ConnectivityErrorType;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

public final class ConnectivityException extends RuntimeException {

    private static final AccountInformationErrorDefaultMessageMapper
            ACCOUNT_INFORMATION_ERROR_DEFAULT_MESSAGE_PROVIDER =
                    new AccountInformationErrorDefaultMessageMapper();
    private static final AuthorizationErrorDefaultMessageMapper
            AUTHORIZATION_ERROR_DEFAULT_MESSAGE_PROVIDER =
                    new AuthorizationErrorDefaultMessageMapper();
    private static final ProviderErrorDefaultMessageMapper PROVIDER_ERROR_DEFAULT_MESSAGE_PROVIDER =
            new ProviderErrorDefaultMessageMapper();
    private static final TinkSideErrorDefaultMessageMapper
            TINK_SIDE_ERROR_DEFAULT_MESSAGE_PROVIDER = new TinkSideErrorDefaultMessageMapper();
    private static final UserLoginErrorDefaultMessageMapper
            USER_LOGIN_ERROR_DEFAULT_MESSAGE_PROVIDER = new UserLoginErrorDefaultMessageMapper();

    @Getter private final ConnectivityError error;
    @Getter private final LocalizableKey userMessage;
    @With private final Throwable cause;
    @With private final String internalMessage;

    public ConnectivityException(ConnectivityErrorDetails.AccountInformationErrors reason) {
        this(
                ConnectivityErrorType.ACCOUNT_INFORMATION_ERROR,
                reason.name(),
                ACCOUNT_INFORMATION_ERROR_DEFAULT_MESSAGE_PROVIDER.map(reason));
    }

    public ConnectivityException(ConnectivityErrorDetails.AuthorizationErrors reason) {
        this(
                ConnectivityErrorType.AUTHORIZATION_ERROR,
                reason.name(),
                AUTHORIZATION_ERROR_DEFAULT_MESSAGE_PROVIDER.map(reason));
    }

    public ConnectivityException(ConnectivityErrorDetails.ProviderErrors reason) {
        this(
                ConnectivityErrorType.PROVIDER_ERROR,
                reason.name(),
                PROVIDER_ERROR_DEFAULT_MESSAGE_PROVIDER.map(reason));
    }

    public ConnectivityException(ConnectivityErrorDetails.TinkSideErrors reason) {
        this(
                ConnectivityErrorType.TINK_SIDE_ERROR,
                reason.name(),
                TINK_SIDE_ERROR_DEFAULT_MESSAGE_PROVIDER.map(reason));
    }

    public ConnectivityException(ConnectivityErrorDetails.UserLoginErrors reason) {
        this(
                ConnectivityErrorType.USER_LOGIN_ERROR,
                reason.name(),
                USER_LOGIN_ERROR_DEFAULT_MESSAGE_PROVIDER.map(reason));
    }

    private ConnectivityException(
            ConnectivityErrorType errorType, String reasonName, LocalizableKey userMessage) {
        this(
                constructConnectivityError(errorType, reasonName, userMessage),
                userMessage,
                null,
                null);
    }

    private ConnectivityException(
            @NonNull ConnectivityError error,
            LocalizableKey userMessage,
            Throwable cause,
            String internalMessage) {
        super(decideOnMessage(error, internalMessage), cause);
        this.error = error;
        this.userMessage = userMessage;
        this.cause = cause;
        this.internalMessage = internalMessage;
    }

    public ConnectivityException withUserMessage(LocalizableKey userMessage) {
        return new ConnectivityException(
                constructConnectivityError(
                        error.getType(), error.getDetails().getReason(), userMessage),
                userMessage,
                cause,
                internalMessage);
    }

    private static String decideOnMessage(
            @NonNull ConnectivityError error, String internalMessage) {
        return internalMessage != null
                ? internalMessage
                : String.format("Cause: %s.%s", error.getType(), error.getDetails().getReason());
    }

    private static ConnectivityError constructConnectivityError(
            ConnectivityErrorType errorType, String reasonName, LocalizableKey userMessage) {
        return ConnectivityError.newBuilder()
                .setType(errorType)
                .setDetails(ConnectivityErrorDetails.newBuilder().setReason(reasonName).build())
                .setDisplayMessage(userMessage.get())
                .build();
    }
}
