package se.tink.backend.aggregation.agents.exceptions.connectivity;

import static se.tink.connectivity.errors.ConnectivityErrorDetails.AuthorizationErrors.ACTION_NOT_PERMITTED;
import static se.tink.connectivity.errors.ConnectivityErrorDetails.AuthorizationErrors.SESSION_EXPIRED;
import static se.tink.connectivity.errors.ConnectivityErrorDetails.AuthorizationErrors.USER_ACTION_REQUIRED;
import static se.tink.connectivity.errors.ConnectivityErrorDetails.AuthorizationErrors.USER_ACTION_REQUIRED_UNSIGNED_AGREEMENT;

import com.google.common.collect.ImmutableMap;
import se.tink.connectivity.errors.ConnectivityErrorDetails;
import se.tink.libraries.i18n.LocalizableKey;

final class AuthorizationErrorDefaultMessageProvider
        implements ConnectivityErrorDefaultMessageProvider<
                ConnectivityErrorDetails.AuthorizationErrors> {

    private static final LocalizableKey DEFAULT_MESSAGE =
            new LocalizableKey("Authorization error.");

    private static final ImmutableMap<ConnectivityErrorDetails.AuthorizationErrors, LocalizableKey>
            AUTHORIZATION_ERRORS_USER_MESSAGES_MAP =
                    ImmutableMap
                            .<ConnectivityErrorDetails.AuthorizationErrors, LocalizableKey>builder()
                            .put(
                                    ACTION_NOT_PERMITTED,
                                    new LocalizableKey(
                                            "You are not authorized to use this service."))
                            .put(
                                    SESSION_EXPIRED,
                                    new LocalizableKey(
                                            "For safety reasons you have been logged out. Please login again to continue."))
                            .put(
                                    USER_ACTION_REQUIRED_UNSIGNED_AGREEMENT,
                                    new LocalizableKey(
                                            "You do not have access to mobile banking. Please contact your bank."))
                            .put(
                                    USER_ACTION_REQUIRED,
                                    new LocalizableKey(
                                            "Could not login to your bank. The access could be blocked. Please activate it in your bank app or contact your bank."))
                            .build();

    @Override
    public LocalizableKey provide(ConnectivityErrorDetails.AuthorizationErrors reason) {
        return AUTHORIZATION_ERRORS_USER_MESSAGES_MAP.getOrDefault(reason, DEFAULT_MESSAGE);
    }
}
