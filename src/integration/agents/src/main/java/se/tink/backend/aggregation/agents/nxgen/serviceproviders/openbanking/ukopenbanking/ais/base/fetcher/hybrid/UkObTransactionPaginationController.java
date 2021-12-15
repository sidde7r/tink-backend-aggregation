package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher.hybrid;

import com.google.common.collect.Lists;
import java.time.OffsetDateTime;
import java.time.Period;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.hybrid.TransactionOffsetDateTimeKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@Slf4j
@RequiredArgsConstructor
public class UkObTransactionPaginationController<ACCOUNT extends Account>
        implements TransactionPaginator<ACCOUNT> {

    private final TransactionOffsetDateTimeKeyPaginator<ACCOUNT, String> paginator;
    private final UkObDateCalculator<ACCOUNT> ukCalculator;

    private final Period maxPeriodForSingleCycle;

    private OffsetDateTime toDateTime;
    private OffsetDateTime fromDateTime;
    private OffsetDateTime finalFromDateTime;

    @Override
    public void resetState() {
        toDateTime = null;
        fromDateTime = null;
        finalFromDateTime = null;
    }

    @Override
    public PaginatorResponse fetchTransactionsFor(ACCOUNT account) {
        toDateTime = ukCalculator.calculateTo(fromDateTime);
        calculateFinalFromDateIfUninitialised(account);
        fromDateTime =
                ukCalculator.calculateFromAsStartOfTheDayWithLimit(
                        toDateTime, maxPeriodForSingleCycle, finalFromDateTime);

        TransactionKeyPaginatorResponse<String> response =
                paginator.getTransactionsFor(account, fromDateTime, toDateTime);
        List<Transaction> transactions = Lists.newArrayList(response.getTinkTransactions());

        while (canFetchMoreByKey(response)) {
            response = paginator.getTransactionsFor(account, response.nextKey());
            transactions.addAll(response.getTinkTransactions());
        }

        return PaginatorResponseImpl.create(transactions, !isFinalFromDateReached());
    }

    private void calculateFinalFromDateIfUninitialised(ACCOUNT account) {
        if (finalFromDateTime == null) {
            finalFromDateTime = ukCalculator.calculateFinalFromDate(account, toDateTime);
            log.info("[TRANSACTION FETCHING] Calculated finalFromDateTime: {}", finalFromDateTime);
        }
    }

    private boolean canFetchMoreByKey(TransactionKeyPaginatorResponse<String> response) {
        return response.canFetchMore().orElse(Boolean.FALSE);
    }

    private boolean isFinalFromDateReached() {
        return fromDateTime.isEqual(finalFromDateTime);
    }
}
