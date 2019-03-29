package se.tink.backend.nasa.boot.rpc;

import java.util.Arrays;
import java.util.List;

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
    INVESTMENT_TRANSACTIONS(AccountTypes.INVESTMENT);

    private final List<AccountTypes> accountTypes;

    RefreshableItem(AccountTypes... types) {
        accountTypes = Arrays.asList(types);
    }
}
