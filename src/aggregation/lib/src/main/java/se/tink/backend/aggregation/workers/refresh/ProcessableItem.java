package se.tink.backend.aggregation.workers.refresh;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ordering;
import java.util.List;
import java.util.Set;
import se.tink.libraries.credentials.service.RefreshableItem;

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

    // Explicit order of processable items
    private static final Ordering<ProcessableItem> PROCESSABLE_ITEM_ORDERING = Ordering.explicit(ImmutableList.of(
            ACCOUNTS,
            EINVOICES,
            TRANSFER_DESTINATIONS,
            TRANSACTIONS));

    public static List<ProcessableItem> sort(Set<ProcessableItem> items) {
        return PROCESSABLE_ITEM_ORDERING.sortedCopy(items);
    }
}
