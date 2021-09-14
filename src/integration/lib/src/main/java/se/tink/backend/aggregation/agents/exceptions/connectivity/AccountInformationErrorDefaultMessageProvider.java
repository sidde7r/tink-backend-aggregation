package se.tink.backend.aggregation.agents.exceptions.connectivity;

import static se.tink.connectivity.errors.ConnectivityErrorDetails.AccountInformationErrors.NO_ACCOUNTS;

import com.google.common.collect.ImmutableMap;
import se.tink.connectivity.errors.ConnectivityErrorDetails;
import se.tink.libraries.i18n.LocalizableKey;

final class AccountInformationErrorDefaultMessageProvider
        implements ConnectivityErrorDefaultMessageProvider<
                ConnectivityErrorDetails.AccountInformationErrors> {

    private static final LocalizableKey DEFAULT_MESSAGE =
            new LocalizableKey("Could not retrieve information about an account.");

    private static final ImmutableMap<
                    ConnectivityErrorDetails.AccountInformationErrors, LocalizableKey>
            ACCOUNT_INFORMATION_ERRORS_USER_MESSAGES_MAP =
                    ImmutableMap
                            .<ConnectivityErrorDetails.AccountInformationErrors, LocalizableKey>
                                    builder()
                            .put(
                                    NO_ACCOUNTS,
                                    new LocalizableKey(
                                            "You do not have any accounts available for online access."))
                            .build();

    @Override
    public LocalizableKey provide(ConnectivityErrorDetails.AccountInformationErrors reason) {
        return ACCOUNT_INFORMATION_ERRORS_USER_MESSAGES_MAP.getOrDefault(reason, DEFAULT_MESSAGE);
    }
}
