package se.tink.backend.aggregation.agents.nxgen.demo.banks.demofakebank.fetcher.transactionalaccount;

import java.util.Collections;
import java.util.Date;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class DemoFakeBankTransactionFetcher {

    public DemoFakeBankTransactionFetcher() {}

    public TransactionKeyPaginatorResponse<Date> fetchTransactionsFor(
            TransactionalAccount account, Date date) {

        TransactionKeyPaginatorResponseImpl<Date> response =
                new TransactionKeyPaginatorResponseImpl<>();

        // TODO: get real transactions from bank via apiClient.fetchTransactions()
        response.setTransactions(Collections.emptyList());

        return response;
    }
}
