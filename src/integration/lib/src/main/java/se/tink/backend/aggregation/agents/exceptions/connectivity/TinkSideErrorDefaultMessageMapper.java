package se.tink.backend.aggregation.agents.exceptions.connectivity;

import static se.tink.connectivity.errors.ConnectivityErrorDetails.TinkSideErrors.AUTHENTICATION_METHOD_NOT_SUPPORTED;
import static se.tink.connectivity.errors.ConnectivityErrorDetails.TinkSideErrors.OPERATION_NOT_SUPPORTED;
import static se.tink.connectivity.errors.ConnectivityErrorDetails.TinkSideErrors.TIMEOUT;
import static se.tink.connectivity.errors.ConnectivityErrorDetails.TinkSideErrors.TINK_INTERNAL_SERVER_ERROR;
import static se.tink.connectivity.errors.ConnectivityErrorDetails.TinkSideErrors.UNKNOWN_ERROR;

import com.google.common.collect.ImmutableMap;
import se.tink.connectivity.errors.ConnectivityErrorDetails;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

final class TinkSideErrorDefaultMessageMapper
        implements ConnectivityErrorDefaultMessageMapper<ConnectivityErrorDetails.TinkSideErrors> {

    private static final LocalizableKey DEFAULT_MESSAGE =
            new LocalizableKey("A temporary problem has occurred. Please retry later.");

    private static final ImmutableMap<ConnectivityErrorDetails.TinkSideErrors, LocalizableKey>
            TINK_SIDE_ERRORS_USER_MESSAGES_MAP =
                    ImmutableMap.<ConnectivityErrorDetails.TinkSideErrors, LocalizableKey>builder()
                            .put(
                                    UNKNOWN_ERROR,
                                    new LocalizableKey(
                                            "A problem has occurred. Please retry later."))
                            .put(
                                    TINK_INTERNAL_SERVER_ERROR,
                                    new LocalizableKey(
                                            "A problem has occurred. Please retry later."))
                            .put(
                                    OPERATION_NOT_SUPPORTED,
                                    new LocalizableKey(
                                            "You have chosen an invalid authentication method. Please choose another authentication method."))
                            .put(
                                    AUTHENTICATION_METHOD_NOT_SUPPORTED,
                                    new LocalizableKey(
                                            "You have chosen an unsupported authentication method. Please go back and choose another authentication method."))
                            .put(
                                    TIMEOUT,
                                    new LocalizableKey(
                                            "Your connection has timed out. Please retry."))
                            .build();

    @Override
    public LocalizableKey map(ConnectivityErrorDetails.TinkSideErrors reason) {
        return TINK_SIDE_ERRORS_USER_MESSAGES_MAP.getOrDefault(reason, DEFAULT_MESSAGE);
    }
}
