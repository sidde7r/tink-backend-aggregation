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
import se.tink.libraries.i18n.LocalizableKey;

final class UserLoginErrorDefaultMessageProvider
        implements ConnectivityErrorDefaultMessageProvider<
                ConnectivityErrorDetails.UserLoginErrors> {

    private static final LocalizableKey DEFAULT_MESSAGE =
            new LocalizableKey("Something went wrong during login process.");

    private static final ImmutableMap<ConnectivityErrorDetails.UserLoginErrors, LocalizableKey>
            USER_LOGIN_ERRORS_USER_MESSAGES_MAP =
                    ImmutableMap.<ConnectivityErrorDetails.UserLoginErrors, LocalizableKey>builder()
                            .put(
                                    THIRD_PARTY_AUTHENTICATION_UNAVAILABLE,
                                    new LocalizableKey(
                                            "The bank service has temporarily failed; please try again later."))
                            .put(
                                    STATIC_CREDENTIALS_INCORRECT,
                                    new LocalizableKey(
                                            "Incorrect login credentials. Please try again."))
                            .put(
                                    DYNAMIC_CREDENTIALS_INCORRECT,
                                    new LocalizableKey(
                                            "Incorrect challenge response. Please try again."))
                            .put(
                                    DYNAMIC_CREDENTIALS_FLOW_CANCELLED,
                                    new LocalizableKey(
                                            "Authentication was cancelled. Please try again."))
                            .put(
                                    DYNAMIC_CREDENTIALS_FLOW_TIMEOUT,
                                    new LocalizableKey("Authentication timed out."))
                            .put(
                                    USER_NOT_A_CUSTOMER,
                                    new LocalizableKey(
                                            "You don't have any commitments in the selected bank."))
                            .put(
                                    USER_CONCURRENT_LOGINS,
                                    new LocalizableKey(
                                            "Another client is already trying to sign in."))
                            .put(
                                    USER_BLOCKED,
                                    new LocalizableKey(
                                            "Could not login to your bank. The access could be blocked. Please activate it in your bank app or contact your bank."))
                            .build();

    @Override
    public LocalizableKey provide(ConnectivityErrorDetails.UserLoginErrors reason) {
        return USER_LOGIN_ERRORS_USER_MESSAGES_MAP.getOrDefault(reason, DEFAULT_MESSAGE);
    }
}
