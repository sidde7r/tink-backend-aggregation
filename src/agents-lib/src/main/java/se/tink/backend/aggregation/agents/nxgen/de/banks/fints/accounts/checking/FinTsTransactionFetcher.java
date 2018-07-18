package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.accounts.checking;

import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.statement.MT940Statement;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

public class FinTsTransactionFetcher implements TransactionDatePaginator<TransactionalAccount> {

    private FinTsApiClient apiClient;

    public FinTsTransactionFetcher(FinTsApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<? extends Transaction> getTransactionsFor(TransactionalAccount account, Date fromDate,
            Date toDate) {
        return apiClient.getTransactions(account.getAccountNumber(), fromDate, toDate).stream()
                .map(MT940Statement::toTinkTransaction)
                .collect(Collectors.toList());
    }

    // TODO upcoming transactions
}
