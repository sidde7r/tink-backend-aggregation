package se.tink.libraries.credentials.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ordering;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import se.tink.backend.agents.rpc.AccountTypes;

public enum RefreshableItem {
    ACCOUNTS,
    TRANSACTIONAL_ACCOUNTS_AND_TRANSACTIONS,
    EINVOICES,
    TRANSFER_DESTINATIONS,

    CHECKING_ACCOUNTS(AccountTypes.CHECKING, AccountTypes.OTHER),
    CHECKING_TRANSACTIONS(AccountTypes.CHECKING, AccountTypes.OTHER),
    SAVING_ACCOUNTS(AccountTypes.SAVINGS, AccountTypes.PENSION),
    SAVING_TRANSACTIONS(AccountTypes.SAVINGS, AccountTypes.PENSION),
    CREDITCARD_ACCOUNTS(AccountTypes.CREDIT_CARD),
    CREDITCARD_TRANSACTIONS(AccountTypes.CREDIT_CARD),
    LOAN_ACCOUNTS(AccountTypes.LOAN, AccountTypes.MORTGAGE),
    LOAN_TRANSACTIONS(AccountTypes.LOAN, AccountTypes.MORTGAGE),
    INVESTMENT_ACCOUNTS(AccountTypes.INVESTMENT),
    INVESTMENT_TRANSACTIONS(AccountTypes.INVESTMENT),

    IDENTITY_DATA;

    private final List<AccountTypes> accountTypes;

    RefreshableItem(AccountTypes... types) {
        accountTypes = Arrays.asList(types);
    }

    // Explicit order of refreshable items. Many subsequent places assumes Accounts will come first.
    private static final Ordering<RefreshableItem> REFRESHABLE_ITEM_ORDERING =
            Ordering.explicit(
                    ImmutableList.of(
                            RefreshableItem.CHECKING_ACCOUNTS,
                            RefreshableItem.SAVING_ACCOUNTS,
                            RefreshableItem.CREDITCARD_ACCOUNTS,
                            RefreshableItem.LOAN_ACCOUNTS,
                            RefreshableItem.INVESTMENT_ACCOUNTS,
                            RefreshableItem.CHECKING_TRANSACTIONS,
                            RefreshableItem.SAVING_TRANSACTIONS,
                            RefreshableItem.CREDITCARD_TRANSACTIONS,
                            RefreshableItem.LOAN_TRANSACTIONS,
                            RefreshableItem.INVESTMENT_TRANSACTIONS,
                            RefreshableItem.EINVOICES,
                            RefreshableItem.TRANSFER_DESTINATIONS,
                            RefreshableItem.ACCOUNTS,
                            RefreshableItem.TRANSACTIONAL_ACCOUNTS_AND_TRANSACTIONS,
                            RefreshableItem.IDENTITY_DATA));

    public static final ImmutableSet<RefreshableItem> REFRESHABLE_ITEMS_ACCOUNTS =
            ImmutableSet.<RefreshableItem>builder()
                    .add(RefreshableItem.CHECKING_ACCOUNTS)
                    .add(RefreshableItem.SAVING_ACCOUNTS)
                    .add(RefreshableItem.CREDITCARD_ACCOUNTS)
                    .add(RefreshableItem.LOAN_ACCOUNTS)
                    .add(RefreshableItem.INVESTMENT_ACCOUNTS)
                    .build();

    public static final ImmutableSet<RefreshableItem> REFRESHABLE_ITEMS_TRANSACTIONS =
            ImmutableSet.<RefreshableItem>builder()
                    .add(RefreshableItem.CHECKING_TRANSACTIONS)
                    .add(RefreshableItem.SAVING_TRANSACTIONS)
                    .add(RefreshableItem.CREDITCARD_TRANSACTIONS)
                    .add(RefreshableItem.LOAN_TRANSACTIONS)
                    .add(RefreshableItem.INVESTMENT_TRANSACTIONS)
                    .build();

    // Legacy items not included
    public static final ImmutableSet<RefreshableItem> REFRESHABLE_ITEMS_ALL =
            ImmutableSet.<RefreshableItem>builder()
                    .add(RefreshableItem.CHECKING_ACCOUNTS)
                    .add(RefreshableItem.CHECKING_TRANSACTIONS)
                    .add(RefreshableItem.SAVING_ACCOUNTS)
                    .add(RefreshableItem.SAVING_TRANSACTIONS)
                    .add(RefreshableItem.CREDITCARD_ACCOUNTS)
                    .add(RefreshableItem.CREDITCARD_TRANSACTIONS)
                    .add(RefreshableItem.LOAN_ACCOUNTS)
                    .add(RefreshableItem.LOAN_TRANSACTIONS)
                    .add(RefreshableItem.INVESTMENT_ACCOUNTS)
                    .add(RefreshableItem.INVESTMENT_TRANSACTIONS)
                    .add(RefreshableItem.EINVOICES)
                    .add(RefreshableItem.TRANSFER_DESTINATIONS)
                    .add(RefreshableItem.IDENTITY_DATA)
                    .build();

    public boolean isAccountType(AccountTypes type) {
        return accountTypes.contains(type);
    }

    public static List<RefreshableItem> sort(Set<RefreshableItem> items) {
        return REFRESHABLE_ITEM_ORDERING.sortedCopy(items);
    }

    public static boolean isAccount(RefreshableItem item) {
        return REFRESHABLE_ITEMS_ACCOUNTS.contains(item);
    }

    public static boolean hasAccounts(Collection<RefreshableItem> items) {
        return !Collections.disjoint(items, REFRESHABLE_ITEMS_ACCOUNTS);
    }

    public static boolean hasTransactions(Collection<RefreshableItem> items) {
        return !Collections.disjoint(items, REFRESHABLE_ITEMS_TRANSACTIONS);
    }

    public static Set<RefreshableItem> convertLegacyItems(Set<RefreshableItem> items) {
        if (items.contains(RefreshableItem.ACCOUNTS)) {
            items.remove(RefreshableItem.ACCOUNTS);
            items.addAll(RefreshableItem.REFRESHABLE_ITEMS_ACCOUNTS);
        }

        if (items.contains(RefreshableItem.TRANSACTIONAL_ACCOUNTS_AND_TRANSACTIONS)) {
            items.remove(RefreshableItem.TRANSACTIONAL_ACCOUNTS_AND_TRANSACTIONS);
            items.addAll(RefreshableItem.REFRESHABLE_ITEMS_TRANSACTIONS);
        }

        return items;
    }
}
