package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.transactional;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.N26ApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.rpc.AccountTypes;

public class N26TransactionFetcher implements TransactionKeyPaginator<TransactionalAccount, String>{

    private final N26ApiClient n26ApiClient;

    public N26TransactionFetcher(N26ApiClient n26ApiClient){
        this.n26ApiClient = n26ApiClient;
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(TransactionalAccount account, String key) {
        if(account.getType() == AccountTypes.CHECKING){
            return n26ApiClient.fetchTransactions();
        }

        return new TransactionKeyPaginatorResponseImpl<>();
    }
}
