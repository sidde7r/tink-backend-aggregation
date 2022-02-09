package se.tink.backend.aggregation.agents.exceptions.connectivity;

import static se.tink.connectivity.errors.ConnectivityErrorDetails.UserLoginErrors.DYNAMIC_CREDENTIALS_FLOW_CANCELLED;
import static se.tink.connectivity.errors.ConnectivityErrorDetails.UserLoginErrors.DYNAMIC_CREDENTIALS_FLOW_TIMEOUT;
import static se.tink.connectivity.errors.ConnectivityErrorDetails.UserLoginErrors.DYNAMIC_CREDENTIALS_INCORRECT;
import static se.tink.connectivity.errors.ConnectivityErrorDetails.UserLoginErrors.STATIC_CREDENTIALS_INCORRECT;
import static se.tink.connectivity.errors.ConnectivityErrorDetails.UserLoginErrors.THIRD_PARTY_AUTHENTICATION_UNAVAILABLE;
import static se.tink.connectivity.errors.ConnectivityErrorDetails.UserLoginErrors.USER_BLOCKED;
import static se.tink.connectivity.errors.ConnectivityErrorDetails.UserLoginErrors.USER_CONCURRENT_LOGINS;
import static se.tink.connectivity.errors.ConnectivityErrorDetails.UserLoginErrors.USER_NOT_A_CUSTOMER;

import com.google.common.collect.ImmutableMap;
import se.tink.connectivity.errors.ConnectivityErrorDetails;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

final class UserLoginErrorDefaultMessageMapper
        implements ConnectivityErrorDefaultMessageMapper<ConnectivityErrorDetails.UserLoginErrors> {

    private static final LocalizableKey DEFAULT_MESSAGE =
            new LocalizableKey("A temporary problem has occurred. Please retry later.");

    private static final ImmutableMap<ConnectivityErrorDetails.UserLoginErrors, LocalizableKey>
            USER_LOGIN_ERRORS_USER_MESSAGES_MAP =
                    ImmutableMap.<ConnectivityErrorDetails.UserLoginErrors, LocalizableKey>builder()
                            .put(
                                    THIRD_PARTY_AUTHENTICATION_UNAVAILABLE,
                                    new LocalizableKey(
                                            "The authentication method can not be used. Please go back and choose another method or retry later."))
                            .put(
                                    STATIC_CREDENTIALS_INCORRECT,
                                    new LocalizableKey(
                                            "You have entered the wrong user name or/and password. Please try to log in again."))
                            .put(
                                    DYNAMIC_CREDENTIALS_INCORRECT,
                                    new LocalizableKey(
                                            "Your one-time password is incorrect. Please retry."))
                            .put(
                                    DYNAMIC_CREDENTIALS_FLOW_CANCELLED,
                                    new LocalizableKey(
                                            "You have cancelled authentication. Please retry."))
                            .put(
                                    DYNAMIC_CREDENTIALS_FLOW_TIMEOUT,
                                    new LocalizableKey(
                                            "Your connection has timed out. Please retry."))
                            .put(
                                    USER_NOT_A_CUSTOMER,
                                    new LocalizableKey(
                                            "You can not log in. The bank you selected does not accept your choice. Please select another bank or contact your bank."))
                            .put(
                                    USER_CONCURRENT_LOGINS,
                                    new LocalizableKey(
                                            "You are already logged in. Please log out and retry."))
                            .put(
                                    USER_BLOCKED,
                                    new LocalizableKey(
                                            "You can not log in with your bank. Your account may be blocked from logging in. Please contact your bank."))
                            .build();

    @Override
    public LocalizableKey map(ConnectivityErrorDetails.UserLoginErrors reason) {
        return USER_LOGIN_ERRORS_USER_MESSAGES_MAP.getOrDefault(reason, DEFAULT_MESSAGE);
    }
}
