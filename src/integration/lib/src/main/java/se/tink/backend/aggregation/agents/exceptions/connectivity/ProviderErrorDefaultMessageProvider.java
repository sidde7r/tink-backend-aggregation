package se.tink.backend.aggregation.agents.exceptions.connectivity;

import static se.tink.connectivity.errors.ConnectivityErrorDetails.ProviderErrors.LICENSED_PARTY_REJECTED;
import static se.tink.connectivity.errors.ConnectivityErrorDetails.ProviderErrors.PROVIDER_UNAVAILABLE;

import com.google.common.collect.ImmutableMap;
import se.tink.connectivity.errors.ConnectivityErrorDetails;
import se.tink.libraries.i18n.LocalizableKey;

final class ProviderErrorDefaultMessageProvider
        implements ConnectivityErrorDefaultMessageProvider<
                ConnectivityErrorDetails.ProviderErrors> {

    private static final LocalizableKey DEFAULT_MESSAGE =
            new LocalizableKey("Something went wrong during the process.");

    private static final ImmutableMap<ConnectivityErrorDetails.ProviderErrors, LocalizableKey>
            PROVIDER_ERRORS_USER_MESSAGES_MAP =
                    ImmutableMap.<ConnectivityErrorDetails.ProviderErrors, LocalizableKey>builder()
                            .put(
                                    PROVIDER_UNAVAILABLE,
                                    new LocalizableKey(
                                            "The bank service is offline; please try again later."))
                            .put(
                                    LICENSED_PARTY_REJECTED,
                                    new LocalizableKey(
                                            "Permission denied. Please verify if configuration of secrets you set for that financial institution is correct."))
                            .build();

    @Override
    public LocalizableKey provide(ConnectivityErrorDetails.ProviderErrors reason) {
        return PROVIDER_ERRORS_USER_MESSAGES_MAP.getOrDefault(reason, DEFAULT_MESSAGE);
    }
}
