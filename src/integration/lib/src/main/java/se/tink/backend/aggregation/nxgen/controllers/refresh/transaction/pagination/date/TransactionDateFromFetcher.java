package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date;

import java.time.LocalDate;
import java.util.List;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

public interface TransactionDateFromFetcher<A extends Account> {

    List<? extends AggregationTransaction> fetchTransactionsFor(A account, LocalDate dateFrom);

    LocalDate minimalFromDate();
}
