package se.tink.backend.aggregation.workers.refresh;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import se.tink.backend.aggregation.rpc.RefreshableItem;

public enum ProcessableItem {
    ACCOUNTS,
    TRANSACTIONS,
    EINVOICES,
    TRANSFER_DESTINATIONS;

    public String asMetricValue() {
        return name().toLowerCase();
    }

    public static Set<ProcessableItem> fromRefreshableItems(Set<RefreshableItem> refreshableItems) {
        ImmutableSet.Builder<ProcessableItem> builder = ImmutableSet.builder();

        if (RefreshableItem.hasAccounts(refreshableItems)) {
            builder.add(ProcessableItem.ACCOUNTS);
        }

        if (refreshableItems.contains(RefreshableItem.EINVOICES)) {
            builder.add(ProcessableItem.EINVOICES);
        }

        if (refreshableItems.contains(RefreshableItem.TRANSFER_DESTINATIONS)) {
            builder.add(ProcessableItem.TRANSFER_DESTINATIONS);
        }

        // Transactions are processed last of the refreshable items since the credential status will
        // be set `UPDATED`
        // by system when the processing is done.
        if (RefreshableItem.hasTransactions(refreshableItems)) {
            builder.add(ProcessableItem.TRANSACTIONS);
        }

        return builder.build();
    }
}
