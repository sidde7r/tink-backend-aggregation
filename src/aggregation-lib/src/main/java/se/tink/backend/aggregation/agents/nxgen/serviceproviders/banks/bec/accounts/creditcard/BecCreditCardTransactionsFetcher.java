package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.creditcard;

import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.checking.entities.RecordEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class BecCreditCardTransactionsFetcher implements TransactionDatePaginator<CreditCardAccount> {

    private final BecApiClient apiClient;

    public BecCreditCardTransactionsFetcher(BecApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<? extends Transaction> getTransactionsFor(CreditCardAccount account, Date fromDate, Date toDate) {
        return Optional.of(apiClient.fetchAccountTransactions(account, fromDate, toDate).getRecord())
                .orElseThrow(() -> new IllegalStateException("No records")).stream()
                .map(RecordEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }
}
