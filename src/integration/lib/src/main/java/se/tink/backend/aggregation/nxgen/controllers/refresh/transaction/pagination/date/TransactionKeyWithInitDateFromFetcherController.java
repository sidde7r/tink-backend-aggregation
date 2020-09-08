package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date;

import com.google.common.collect.Lists;
import java.time.LocalDate;
import java.util.List;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class TransactionKeyWithInitDateFromFetcherController<A extends Account, T>
        implements TransactionFetcher<A> {

    private final CredentialsRequest credentialsRequest;
    private final KeyWithInitiDateFromFetcher fetcher;

    public TransactionKeyWithInitDateFromFetcherController(
            CredentialsRequest credentialsRequest, KeyWithInitiDateFromFetcher fetcher) {
        this.credentialsRequest = credentialsRequest;
        this.fetcher = fetcher;
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(A account) {

        List<AggregationTransaction> transactions = Lists.newArrayList();

        TransactionKeyPaginatorResponse<T> result =
                fetcher.fetchTransactionsFor(account, calculateDateFrom(account));
        transactions.addAll(result.getTinkTransactions());
        while (result.canFetchMore().isPresent()
                && result.canFetchMore().get()
                && result.nextKey() != null) {
            result = fetcher.getTransactionsFor(account, result.nextKey());
            transactions.addAll(result.getTinkTransactions());
        }
        return transactions;
    }

    private LocalDate calculateDateFrom(Account account) {
        return credentialsRequest.getAccounts().stream()
                .filter(rpcAccount -> account.isUniqueIdentifierEqual(rpcAccount.getBankId()))
                .findAny()
                .map(a -> a.getCertainDate())
                .map(d -> new java.sql.Date(d.getTime()).toLocalDate())
                .filter(date -> date.isAfter(fetcher.minimalFromDate()))
                .orElse(fetcher.minimalFromDate());
    }
}
