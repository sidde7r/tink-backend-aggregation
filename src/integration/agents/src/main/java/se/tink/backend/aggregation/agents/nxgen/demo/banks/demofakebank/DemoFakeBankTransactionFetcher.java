package se.tink.backend.aggregation.agents.nxgen.demo.banks.demofakebank;

import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

import java.util.Collections;
import java.util.Date;

public class DemoFakeBankTransactionFetcher {

    public DemoFakeBankTransactionFetcher() {
    }

    public TransactionKeyPaginatorResponse<Date> fetchTransactionsFor(TransactionalAccount account, Date date) {

        TransactionKeyPaginatorResponseImpl<Date> response = new TransactionKeyPaginatorResponseImpl<>();

        response.setTransactions(Collections.emptyList()); // TODO: get real transactions from bank via apiClient.fetchTransactions()

        return response;
    }
}
