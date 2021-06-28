package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.fetcher.transactionalaccount;

import com.google.common.collect.Lists;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbStorage;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

@AllArgsConstructor
public class DkbTransactionsFetcher implements TransactionFetcher<TransactionalAccount> {

    private final DkbApiClient apiClient;
    private final DkbStorage storage;
    private final boolean isUserPresent;

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(TransactionalAccount account) {
        List<AggregationTransaction> transactions = Lists.newArrayList();

        final GetTransactionsResponse transactionsResponse =
                apiClient.getTransactions(account, getFetchStartDate(), LocalDate.now());

        transactions.addAll(transactionsResponse.toTinkTransactions());
        return transactions;
    }

    private LocalDate getFetchStartDate() {
        LocalDate startDate;
        if (isUserPresent && storage.isFirstFetch()) {
            startDate = LocalDate.ofEpochDay(0);
            storage.markFirstFetchAsDone();
        } else {
            startDate = LocalDate.now().minusDays(89);
        }
        return startDate;
    }
}
