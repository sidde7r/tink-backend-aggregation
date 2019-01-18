package se.tink.backend.aggregation.agents;

import java.util.List;
import se.tink.backend.aggregation.rpc.Account;

public interface RefreshExecutor {
    RefreshEInvoicesResponse refreshEInvoices();
    RefreshTransferDestinationsResponse refreshTransferDestinations(List<Account> updatedAccounts);

    RefreshAccountsResponse refreshCheckingAccounts();
    RefreshAccountsResponse refreshSavingAccounts();
    RefreshAccountsResponse refreshCreditCardAccounts();
    RefreshTransactionsResponse refreshCheckingTransactions();
    RefreshTransactionsResponse refreshSavingTransactions();
    RefreshTransactionsResponse refreshCreditCardTransactions();

    RefreshLoanAccountsResponse refreshLoanAccounts();
    RefreshInvestmentAccountsResponse refreshInvestmentAccounts();
}
