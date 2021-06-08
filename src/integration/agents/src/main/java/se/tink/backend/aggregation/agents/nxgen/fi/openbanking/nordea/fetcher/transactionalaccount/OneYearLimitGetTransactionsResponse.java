package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.nordea.fetcher.transactionalaccount;

import com.google.common.collect.Iterables;
import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;

public class OneYearLimitGetTransactionsResponse
        extends GetTransactionsResponse<NordeaFiTransactionEntity> {

    private LocalDateTimeSource localDateTimeSource;

    @Override
    public Optional<Boolean> canFetchMore() {
        if (containsTransactionOlderThanYear(getResponse().getTransactions())) {
            return Optional.of(false);
        }
        return super.canFetchMore();
    }

    public OneYearLimitGetTransactionsResponse setLocalDateTimeSource(
            LocalDateTimeSource localDateTimeSource) {
        this.localDateTimeSource = localDateTimeSource;
        return this;
    }

    private boolean containsTransactionOlderThanYear(List<NordeaFiTransactionEntity> transactions) {
        if (transactions.isEmpty()) {
            return false;
        }
        return isDateOlderThanOneYear(Iterables.getLast(transactions).getValueDate());
    }

    private boolean isDateOlderThanOneYear(LocalDate dateToCheck) {
        return dateToCheck.isBefore(ChronoLocalDate.from(localDateTimeSource.now().minusYears(1)));
    }
}
