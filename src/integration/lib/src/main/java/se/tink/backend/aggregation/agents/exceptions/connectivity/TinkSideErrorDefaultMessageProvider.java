package se.tink.backend.aggregation.agents.exceptions.connectivity;

import static se.tink.connectivity.errors.ConnectivityErrorDetails.TinkSideErrors.AUTHENTICATION_METHOD_NOT_SUPPORTED;
import static se.tink.connectivity.errors.ConnectivityErrorDetails.TinkSideErrors.OPERATION_NOT_SUPPORTED;
import static se.tink.connectivity.errors.ConnectivityErrorDetails.TinkSideErrors.TIMEOUT;
import static se.tink.connectivity.errors.ConnectivityErrorDetails.TinkSideErrors.TINK_INTERNAL_SERVER_ERROR;
import static se.tink.connectivity.errors.ConnectivityErrorDetails.TinkSideErrors.UNKNOWN_ERROR;

import com.google.common.collect.ImmutableMap;
import se.tink.connectivity.errors.ConnectivityErrorDetails;
import se.tink.libraries.i18n.LocalizableKey;

final class TinkSideErrorDefaultMessageProvider
        implements ConnectivityErrorDefaultMessageProvider<
                ConnectivityErrorDetails.TinkSideErrors> {

    private static final LocalizableKey DEFAULT_MESSAGE =
            new LocalizableKey("Something went wrong.");

    private static final ImmutableMap<ConnectivityErrorDetails.TinkSideErrors, LocalizableKey>
            TINK_SIDE_ERRORS_USER_MESSAGES_MAP =
                    ImmutableMap.<ConnectivityErrorDetails.TinkSideErrors, LocalizableKey>builder()
                            .put(UNKNOWN_ERROR, DEFAULT_MESSAGE)
                            .put(TINK_INTERNAL_SERVER_ERROR, DEFAULT_MESSAGE)
                            .put(OPERATION_NOT_SUPPORTED, new LocalizableKey("???"))
                            .put(
                                    AUTHENTICATION_METHOD_NOT_SUPPORTED,
                                    new LocalizableKey(
                                            "The authentication needed to login to your bank is not supported at the moment. If possible please try another option."))
                            .put(TIMEOUT, new LocalizableKey("Authentication timed out."))
                            .build();

    @Override
    public LocalizableKey provide(ConnectivityErrorDetails.TinkSideErrors reason) {
        return TINK_SIDE_ERRORS_USER_MESSAGES_MAP.getOrDefault(reason, DEFAULT_MESSAGE);
    }
}
