package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankdataApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.rpc.GetTransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;

public class BankdataCreditCardTransactionFetcher implements TransactionPagePaginator<CreditCardAccount> {
    private final BankdataApiClient bankClient;

    public BankdataCreditCardTransactionFetcher(BankdataApiClient bankClient) {
        this.bankClient = bankClient;
    }

    @Override
    public GetTransactionsResponse getTransactionsFor(CreditCardAccount account, int page) {
        GetTransactionsRequest getTransactionsRequest = new GetTransactionsRequest()
                .addAccount(account.getBankIdentifier())
                .setPage(page);

        return bankClient.getTransactions(getTransactionsRequest);
    }
}
