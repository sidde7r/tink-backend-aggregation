package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.nordea.fetcher.transactionalaccount;

import com.google.common.collect.Iterables;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.chrono.ChronoLocalDate;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

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

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return super.getTinkTransactions().stream()
                .filter(this::isTinkTransactionDateNotOlderThanYear)
                .collect(Collectors.toList());
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
        return isDateOlderThanYear(Iterables.getLast(transactions).getValueDate());
    }

    private boolean isTinkTransactionDateNotOlderThanYear(Transaction transaction) {
        return !isDateOlderThanYear(transaction.getDate());
    }

    private boolean isDateOlderThanYear(Date dateToCheck) {
        LocalDate localDate = dateToCheck.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return isDateOlderThanYear(localDate);
    }

    private boolean isDateOlderThanYear(LocalDate dateToCheck) {
        return dateToCheck.isBefore(ChronoLocalDate.from(localDateTimeSource.now().minusYears(1)));
    }
}
