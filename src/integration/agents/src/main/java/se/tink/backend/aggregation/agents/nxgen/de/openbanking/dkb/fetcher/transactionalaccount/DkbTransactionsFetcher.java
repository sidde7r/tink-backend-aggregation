package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.fetcher.transactionalaccount;

import com.google.common.collect.Lists;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbStorage;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

@Slf4j
@AllArgsConstructor
public class DkbTransactionsFetcher implements TransactionFetcher<TransactionalAccount> {

    private final DkbApiClient apiClient;
    private final DkbStorage storage;
    private final boolean isUserAvailableForInteraction;
    private final LocalDateTimeSource localDateTimeSource;

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(TransactionalAccount account) {
        List<AggregationTransaction> transactions = Lists.newArrayList();

        final GetTransactionsResponse transactionsResponse =
                apiClient.getTransactions(
                        account, getFetchStartDate(), localDateTimeSource.now().toLocalDate());

        transactions.addAll(transactionsResponse.toTinkTransactions());
        return transactions;
    }

    private LocalDate getFetchStartDate() {
        LocalDate startDate;
        if (isUserAvailableForInteraction && storage.isFirstFetch()) {
            log.info("isFirstFetch for DKB={}", storage.isFirstFetch());
            startDate = LocalDate.ofEpochDay(89); // change to EpochDay(0) after investigation
            storage.markFirstFetchAsDone();
        } else {
            startDate = localDateTimeSource.now().toLocalDate().minusDays(89);
        }
        return startDate;
    }
}
