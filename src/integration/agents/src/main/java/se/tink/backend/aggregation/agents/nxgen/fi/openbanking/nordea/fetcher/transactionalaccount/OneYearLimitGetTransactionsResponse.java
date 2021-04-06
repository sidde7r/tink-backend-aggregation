package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.nordea.fetcher.transactionalaccount;

import com.google.common.collect.Iterables;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.rpc.GetTransactionsResponse;

public class OneYearLimitGetTransactionsResponse
        extends GetTransactionsResponse<NordeaFiTransactionEntity> {

    @Override
    public Optional<Boolean> canFetchMore() {
        if (containsTransactionOlderThanYear(getResponse().getTransactions())) {
            return Optional.of(false);
        }
        return super.canFetchMore();
    }

    private boolean containsTransactionOlderThanYear(List<NordeaFiTransactionEntity> transactions) {
        if (transactions.isEmpty()) {
            return false;
        }
        return isDateOlderThanOneYear(Iterables.getLast(transactions).getValueDate());
    }

    private boolean isDateOlderThanOneYear(LocalDate dateToCheck) {
        return dateToCheck.isBefore(LocalDate.now().minusYears(1));
    }
}
