package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.nordea.fetcher.creditcard;

import static java.util.stream.Collectors.toList;

import com.google.common.collect.Iterables;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.chrono.ChronoLocalDate;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.creditcard.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.creditcard.rpc.CreditCardTransactionResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class OneYearLimitCreditCardTransactionsResponse extends CreditCardTransactionResponse {

    private LocalDateTimeSource localDateTimeSource;

    @Override
    public Optional<Boolean> canFetchMore() {
        if (containsTransactionOlderThanYear(getTransactionsResponse().getTransactions())) {
            return Optional.of(false);
        }
        return super.canFetchMore();
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return super.getTinkTransactions().stream()
                .filter(this::isTinkTransactionDateNotOlderThanYear)
                .collect(toList());
    }

    public void setLocalDateTimeSource(LocalDateTimeSource localDateTimeSource) {
        this.localDateTimeSource = localDateTimeSource;
    }

    private boolean containsTransactionOlderThanYear(List<TransactionEntity> transactions) {
        if (transactions.isEmpty()) {
            return false;
        }
        return isDateOlderThanYear(
                Iterables.getLast(transactions).toTinkCreditCardTransaction().getDate());
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
