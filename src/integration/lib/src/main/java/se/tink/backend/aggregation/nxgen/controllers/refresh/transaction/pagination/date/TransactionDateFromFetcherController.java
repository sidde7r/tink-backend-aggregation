package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date;

import java.time.LocalDate;
import java.util.List;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class TransactionDateFromFetcherController<A extends Account>
        implements TransactionFetcher<A> {

    private final CredentialsRequest credentialsRequest;
    private final TransactionDateFromFetcher transactionDateFromFetcher;

    public TransactionDateFromFetcherController(
            CredentialsRequest credentialsRequest,
            TransactionDateFromFetcher transactionDateFromFetcher) {
        this.credentialsRequest = credentialsRequest;
        this.transactionDateFromFetcher = transactionDateFromFetcher;
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(A account) {
        return transactionDateFromFetcher.fetchTransactionsFor(account, calculateDateFrom(account));
    }

    private LocalDate calculateDateFrom(Account account) {
        return credentialsRequest.getAccounts().stream()
                .filter(rpcAccount -> account.isUniqueIdentifierEqual(rpcAccount.getBankId()))
                .findAny()
                .map(a -> a.getCertainDate())
                .map(d -> new java.sql.Date(d.getTime()).toLocalDate())
                .orElse(transactionDateFromFetcher.minimalFromDate());
    }
}
