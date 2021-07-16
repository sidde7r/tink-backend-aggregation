package se.tink.backend.aggregation.events;

import com.google.common.collect.ImmutableMap;
import se.tink.eventproducerservice.events.grpc.CredentialsRefreshCommandChainStartedProto.CredentialsRefreshCommandChainStarted.RefreshableItems;
import se.tink.libraries.credentials.service.RefreshableItem;

public class RefreshableItemsConverter {
    private static final ImmutableMap<RefreshableItem, RefreshableItems> REFRESHABLE_ITEMS_MAP =
            ImmutableMap.<RefreshableItem, RefreshableItems>builder()
                    .put(
                            RefreshableItem.CHECKING_ACCOUNTS,
                            RefreshableItems.REFRESHABLE_ITEMS_CHECKING_ACCOUNTS)
                    .put(
                            RefreshableItem.CHECKING_TRANSACTIONS,
                            RefreshableItems.REFRESHABLE_ITEMS_CHECKING_TRANSACTIONS)
                    .put(
                            RefreshableItem.CREDITCARD_ACCOUNTS,
                            RefreshableItems.REFRESHABLE_ITEMS_CREDITCARD_ACCOUNTS)
                    .put(
                            RefreshableItem.CREDITCARD_TRANSACTIONS,
                            RefreshableItems.REFRESHABLE_ITEMS_CREDITCARD_TRANSACTIONS)
                    .put(
                            RefreshableItem.IDENTITY_DATA,
                            RefreshableItems.REFRESHABLE_ITEMS_IDENTITY_DATA)
                    .put(
                            RefreshableItem.INVESTMENT_ACCOUNTS,
                            RefreshableItems.REFRESHABLE_ITEMS_INVESTMENT_ACCOUNTS)
                    .put(
                            RefreshableItem.INVESTMENT_TRANSACTIONS,
                            RefreshableItems.REFRESHABLE_ITEMS_INVESTMENT_TRANSACTIONS)
                    .put(
                            RefreshableItem.LOAN_ACCOUNTS,
                            RefreshableItems.REFRESHABLE_ITEMS_LOAN_ACCOUNTS)
                    .put(
                            RefreshableItem.LOAN_TRANSACTIONS,
                            RefreshableItems.REFRESHABLE_ITEMS_LOAN_TRANSACTIONS)
                    .put(
                            RefreshableItem.SAVING_ACCOUNTS,
                            RefreshableItems.REFRESHABLE_ITEMS_SAVING_ACCOUNTS)
                    .put(
                            RefreshableItem.SAVING_TRANSACTIONS,
                            RefreshableItems.REFRESHABLE_ITEMS_SAVING_TRANSACTIONS)
                    .put(RefreshableItem.ACCOUNTS, RefreshableItems.REFRESHABLE_ITEMS_ACCOUNTS)
                    .put(RefreshableItem.EINVOICES, RefreshableItems.REFRESHABLE_ITEMS_EINVOICES)
                    .put(
                            RefreshableItem.TRANSFER_DESTINATIONS,
                            RefreshableItems.REFRESHABLE_ITEMS_TRANSFER_DESTINATIONS)
                    .put(
                            RefreshableItem.TRANSACTIONAL_ACCOUNTS_AND_TRANSACTIONS,
                            RefreshableItems
                                    .REFRESHABLE_ITEMS_TRANSACTIONAL_ACCOUNTS_AND_TRANSACTIONS)
                    .put(
                            RefreshableItem.LIST_BENEFICIARIES,
                            RefreshableItems.REFRESHABLE_ITEMS_LIST_BENEFICIARIES)
                    .build();

    public static RefreshableItems convert(RefreshableItem item) {
        return REFRESHABLE_ITEMS_MAP.getOrDefault(item, RefreshableItems.REFRESHABLE_ITEMS_UNKNOWN);
    }
}
