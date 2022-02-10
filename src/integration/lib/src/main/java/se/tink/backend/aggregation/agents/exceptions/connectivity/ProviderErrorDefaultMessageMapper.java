package se.tink.backend.aggregation.agents.exceptions.connectivity;

import static se.tink.connectivity.errors.ConnectivityErrorDetails.ProviderErrors.PROVIDER_UNAVAILABLE;

import com.google.common.collect.ImmutableMap;
import se.tink.connectivity.errors.ConnectivityErrorDetails;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

final class ProviderErrorDefaultMessageMapper
        implements ConnectivityErrorDefaultMessageMapper<ConnectivityErrorDetails.ProviderErrors> {

    private static final LocalizableKey DEFAULT_MESSAGE =
            new LocalizableKey("A temporary problem has occurred. Please retry later.");

    private static final ImmutableMap<ConnectivityErrorDetails.ProviderErrors, LocalizableKey>
            PROVIDER_ERRORS_USER_MESSAGES_MAP =
                    ImmutableMap.<ConnectivityErrorDetails.ProviderErrors, LocalizableKey>builder()
                            .put(
                                    PROVIDER_UNAVAILABLE,
                                    new LocalizableKey(
                                            "A temporary problem has occurred with your bank. Please retry later."))
                            .build();

    @Override
    public LocalizableKey map(ConnectivityErrorDetails.ProviderErrors reason) {
        return PROVIDER_ERRORS_USER_MESSAGES_MAP.getOrDefault(reason, DEFAULT_MESSAGE);
    }
}
