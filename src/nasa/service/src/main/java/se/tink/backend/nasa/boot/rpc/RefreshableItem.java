package se.tink.backend.nasa.boot.rpc;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ordering;
import java.util.Arrays;
import java.util.List;

public enum RefreshableItem {
    ACCOUNTS, TRANSACTIONAL_ACCOUNTS_AND_TRANSACTIONS, EINVOICES, TRANSFER_DESTINATIONS,

    CHECKING_ACCOUNTS(AccountTypes.CHECKING, AccountTypes.OTHER),
    CHECKING_TRANSACTIONS(AccountTypes.CHECKING, AccountTypes.OTHER),
    SAVING_ACCOUNTS(AccountTypes.SAVINGS, AccountTypes.PENSION),
    SAVING_TRANSACTIONS(AccountTypes.SAVINGS, AccountTypes.PENSION),
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

    private static final Ordering<RefreshableItem> REFRESHABLE_ITEM_ORDERING = Ordering.explicit(ImmutableList.of(
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
            RefreshableItem.TRANSACTIONAL_ACCOUNTS_AND_TRANSACTIONS
    ));

    public static final ImmutableSet<RefreshableItem> REFRESHABLE_ITEMS_ACCOUNTS = ImmutableSet.<RefreshableItem>builder()
            .add(RefreshableItem.CHECKING_ACCOUNTS)
            .add(RefreshableItem.SAVING_ACCOUNTS)
            .add(RefreshableItem.CREDITCARD_ACCOUNTS)
            .add(RefreshableItem.LOAN_ACCOUNTS)
            .add(RefreshableItem.INVESTMENT_ACCOUNTS)
            .build();

    public static final ImmutableSet<RefreshableItem> REFRESHABLE_ITEMS_TRANSACTIONS = ImmutableSet.<RefreshableItem>builder()
            .add(RefreshableItem.CHECKING_TRANSACTIONS)
            .add(RefreshableItem.SAVING_TRANSACTIONS)
            .add(RefreshableItem.CREDITCARD_TRANSACTIONS)
            .add(RefreshableItem.LOAN_TRANSACTIONS)
            .add(RefreshableItem.INVESTMENT_TRANSACTIONS)
            .build();
}
