package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.rpc.MovementsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionMonthPaginator;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class IngTransactionFetcher<A extends Account> implements TransactionMonthPaginator<A> {
    private final IngApiClient ingApiClient;
    private final IngTransactionMapper<A> transactionMapper;

    public IngTransactionFetcher(
            IngApiClient ingApiClient, IngTransactionMapper<A> transactionMapper) {
        this.ingApiClient = ingApiClient;
        this.transactionMapper = transactionMapper;
    }

    @Override
    public PaginatorResponse getTransactionsFor(A account, Year year, Month month) {
        LocalDate fromDate = LocalDate.of(year.getValue(), month, 1);
        LocalDate toDate = fromDate.with(TemporalAdjusters.lastDayOfMonth());
        int offset = 0;
        MovementsResponse movementsResponse;
        List<Transaction> allTransactions = new ArrayList<>();
        do {
            movementsResponse =
                    ingApiClient.getApiRestProductMovements(
                            account.getApiIdentifier(), fromDate, toDate, offset);
            List<Transaction> transactions =
                    movementsResponse.getElements().stream()
                            .map(element -> transactionMapper.toTinkTransaction(account, element))
                            .collect(Collectors.toList());
            allTransactions.addAll(transactions);
            offset += movementsResponse.getCount();
        } while (movementsResponse.getTotal() > offset);
        return PaginatorResponseImpl.create(allTransactions);
    }
}
