package se.tink.backend.aggregation.agents;

import java.util.List;
import se.tink.backend.aggregation.rpc.Account;

public interface RefreshExecutor {
    FetchEInvoicesResponse fetchEInvoices();
    FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> updatedAccounts);

    FetchAccountsResponse fetchCheckingAccounts();
    FetchAccountsResponse fetchSavingsAccounts();
    FetchAccountsResponse fetchCreditCardAccounts();
    FetchTransactionsResponse fetchCheckingTransactions();
    FetchTransactionsResponse fetchSavingsTransactions();
    FetchTransactionsResponse fetchCreditCardTransactions();

    FetchLoanAccountsResponse fetchLoanAccounts();
    FetchInvestmentAccountsResponse fetchInvestmentAccounts();
}
