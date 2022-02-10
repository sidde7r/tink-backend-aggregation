package se.tink.backend.aggregation.agents.exceptions.connectivity;

import static se.tink.connectivity.errors.ConnectivityErrorDetails.AccountInformationErrors.NOT_ENOUGH_DATA_TO_PROVIDE_PRODUCT;

import com.google.common.collect.ImmutableMap;
import se.tink.connectivity.errors.ConnectivityErrorDetails;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

final class AccountInformationErrorDefaultMessageMapper
        implements ConnectivityErrorDefaultMessageMapper<
                ConnectivityErrorDetails.AccountInformationErrors> {

    private static final LocalizableKey DEFAULT_MESSAGE =
            new LocalizableKey("Your bank account information is not available.");

    private static final ImmutableMap<
                    ConnectivityErrorDetails.AccountInformationErrors, LocalizableKey>
            ACCOUNT_INFORMATION_ERRORS_USER_MESSAGES_MAP =
                    ImmutableMap
                            .<ConnectivityErrorDetails.AccountInformationErrors, LocalizableKey>
                                    builder()
                            .put(
                                    NOT_ENOUGH_DATA_TO_PROVIDE_PRODUCT,
                                    new LocalizableKey(
                                            "There is not enough banking data to display your results."))
                            .build();

    @Override
    public LocalizableKey map(ConnectivityErrorDetails.AccountInformationErrors reason) {
        return ACCOUNT_INFORMATION_ERRORS_USER_MESSAGES_MAP.getOrDefault(reason, DEFAULT_MESSAGE);
    }
}
