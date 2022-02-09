package se.tink.backend.aggregation.agents.exceptions.connectivity;

import static se.tink.connectivity.errors.ConnectivityErrorDetails.AuthorizationErrors.ACTION_NOT_PERMITTED;
import static se.tink.connectivity.errors.ConnectivityErrorDetails.AuthorizationErrors.SESSION_EXPIRED;
import static se.tink.connectivity.errors.ConnectivityErrorDetails.AuthorizationErrors.USER_ACTION_REQUIRED;
import static se.tink.connectivity.errors.ConnectivityErrorDetails.AuthorizationErrors.USER_ACTION_REQUIRED_UNSIGNED_AGREEMENT;

import com.google.common.collect.ImmutableMap;
import se.tink.connectivity.errors.ConnectivityErrorDetails;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

final class AuthorizationErrorDefaultMessageMapper
        implements ConnectivityErrorDefaultMessageMapper<
                ConnectivityErrorDetails.AuthorizationErrors> {

    private static final LocalizableKey DEFAULT_MESSAGE =
            new LocalizableKey("You are not authorized to use this service.");

    private static final ImmutableMap<ConnectivityErrorDetails.AuthorizationErrors, LocalizableKey>
            AUTHORIZATION_ERRORS_USER_MESSAGES_MAP =
                    ImmutableMap
                            .<ConnectivityErrorDetails.AuthorizationErrors, LocalizableKey>builder()
                            .put(
                                    ACTION_NOT_PERMITTED,
                                    new LocalizableKey(
                                            "You are not authorised to use this service. Please contact your bank."))
                            .put(
                                    SESSION_EXPIRED,
                                    new LocalizableKey(
                                            "You have been logged out for security reasons. Please log in again."))
                            .put(
                                    USER_ACTION_REQUIRED_UNSIGNED_AGREEMENT,
                                    new LocalizableKey(
                                            "You can not log in. Please activate online banking in your bank app or contact your bank to resolve this issue."))
                            .put(
                                    USER_ACTION_REQUIRED,
                                    new LocalizableKey(
                                            "You can not log in. Please retry or contact your bank to resolve this issue."))
                            .build();

    @Override
    public LocalizableKey map(ConnectivityErrorDetails.AuthorizationErrors reason) {
        return AUTHORIZATION_ERRORS_USER_MESSAGES_MAP.getOrDefault(reason, DEFAULT_MESSAGE);
    }
}
