package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankdataApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankdataConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.rpc.GetTransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.UpcomingTransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;

public class BankdataTransactionFetcher
        implements TransactionPagePaginator<TransactionalAccount>,
                UpcomingTransactionFetcher<TransactionalAccount> {

    private final BankdataApiClient bankClient;

    public BankdataTransactionFetcher(BankdataApiClient bankClient) {
        this.bankClient = bankClient;
    }

    @Override
    public GetTransactionsResponse getTransactionsFor(TransactionalAccount account, int page) {
        GetTransactionsRequest getTransactionsRequest =
                new GetTransactionsRequest().addAccount(account.getBankIdentifier()).setPage(page);

        return this.bankClient.getTransactions(getTransactionsRequest);
    }

    @Override
    public List<UpcomingTransaction> fetchUpcomingTransactionsFor(TransactionalAccount account) {
        GetTransactionsRequest getTransactionsRequest =
                new GetTransactionsRequest()
                        .addAccount(account.getBankIdentifier())
                        .setPage(BankdataConstants.Fetcher.START_PAGE);

        return this.bankClient
                .getFutureTransactions(getTransactionsRequest)
                .getTinkUpcomingTransactions();
    }
}
