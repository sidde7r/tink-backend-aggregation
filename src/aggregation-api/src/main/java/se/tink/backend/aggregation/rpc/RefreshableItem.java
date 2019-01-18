package se.tink.backend.aggregation.rpc;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ordering;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public enum RefreshableItem {
    ACCOUNTS, TRANSACTIONAL_ACCOUNTS_AND_TRANSACTIONS, EINVOICES, TRANSFER_DESTINATIONS,

    CHECKING_ACCOUNTS(AccountTypes.CHECKING, AccountTypes.OTHER),
    CHECKING_TRANSACTIONS(AccountTypes.CHECKING, AccountTypes.OTHER),
    SAVINGS_ACCOUNTS(AccountTypes.SAVINGS, AccountTypes.PENSION),
    SAVINGS_TRANSACTIONS(AccountTypes.SAVINGS, AccountTypes.PENSION),
    CREDITCARD_ACCOUNTS(AccountTypes.CREDIT_CARD),
    CREDITCARD_TRANSACTIONS(AccountTypes.CREDIT_CARD),
    LOAN_ACCOUNTS(AccountTypes.LOAN, AccountTypes.MORTGAGE),
    LOAN_TRANSACTIONS(AccountTypes.LOAN, AccountTypes.MORTGAGE),
    INVESTMENT_ACCOUNTS(AccountTypes.INVESTMENT),
    INVESTMENT_TRANSACTIONS(AccountTypes.INVESTMENT);

    private final List<AccountTypes> accountTypes;

    RefreshableItem(AccountTypes... types) {
        accountTypes = Arrays.asList(types);
    }

    // Explicit order of refreshable items. Many subsequent places assumes Accounts will come first.
    private static final Ordering<RefreshableItem> REFRESHABLE_ITEM_ORDERING = Ordering.explicit(ImmutableList.of(
            RefreshableItem.CHECKING_ACCOUNTS,
            RefreshableItem.SAVINGS_ACCOUNTS,
            RefreshableItem.CREDITCARD_ACCOUNTS,
            RefreshableItem.LOAN_ACCOUNTS,
            RefreshableItem.INVESTMENT_ACCOUNTS,

            RefreshableItem.CHECKING_TRANSACTIONS,
            RefreshableItem.SAVINGS_TRANSACTIONS,
            RefreshableItem.CREDITCARD_TRANSACTIONS,
            RefreshableItem.LOAN_TRANSACTIONS,
            RefreshableItem.INVESTMENT_TRANSACTIONS,

            RefreshableItem.EINVOICES,
            RefreshableItem.TRANSFER_DESTINATIONS,

            RefreshableItem.ACCOUNTS,
            RefreshableItem.TRANSACTIONAL_ACCOUNTS_AND_TRANSACTIONS
    ));

    public static final ImmutableSet<RefreshableItem> REFRESHABLE_ITEMS_ACCOUNTS = ImmutableSet.<RefreshableItem>builder()
            .add(RefreshableItem.CHECKING_ACCOUNTS)
            .add(RefreshableItem.SAVINGS_ACCOUNTS)
            .add(RefreshableItem.CREDITCARD_ACCOUNTS)
            .add(RefreshableItem.LOAN_ACCOUNTS)
            .add(RefreshableItem.INVESTMENT_ACCOUNTS)
            .build();

    public static final ImmutableSet<RefreshableItem> REFRESHABLE_ITEMS_TRANSACTIONS = ImmutableSet.<RefreshableItem>builder()
            .add(RefreshableItem.CHECKING_TRANSACTIONS)
            .add(RefreshableItem.SAVINGS_TRANSACTIONS)
            .add(RefreshableItem.CREDITCARD_TRANSACTIONS)
            .add(RefreshableItem.LOAN_TRANSACTIONS)
            .add(RefreshableItem.INVESTMENT_TRANSACTIONS)
            .build();

    // Legacy items not included
    public static final ImmutableSet<RefreshableItem> REFRESHABLE_ITEMS_ALL = ImmutableSet.<RefreshableItem>builder()
            .add(RefreshableItem.CHECKING_ACCOUNTS)
            .add(RefreshableItem.CHECKING_TRANSACTIONS)
            .add(RefreshableItem.SAVINGS_ACCOUNTS)
            .add(RefreshableItem.SAVINGS_TRANSACTIONS)
            .add(RefreshableItem.CREDITCARD_ACCOUNTS)
            .add(RefreshableItem.CREDITCARD_TRANSACTIONS)
            .add(RefreshableItem.LOAN_ACCOUNTS)
            .add(RefreshableItem.LOAN_TRANSACTIONS)
            .add(RefreshableItem.INVESTMENT_ACCOUNTS)
            .add(RefreshableItem.INVESTMENT_TRANSACTIONS)
            .add(RefreshableItem.EINVOICES)
            .add(RefreshableItem.TRANSFER_DESTINATIONS)
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

}
