package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher;

import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngUtils;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.responses.MovementsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.responses.entities.Element;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionMonthPaginator;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class IngTransactionFetcher implements TransactionMonthPaginator {

    private final IngApiClient ingApiClient;

    public IngTransactionFetcher(IngApiClient ingApiClient) {
        this.ingApiClient = ingApiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(Account account, Year year, Month month) {
        LocalDate fromDate = LocalDate.of(year.getValue(), month, 1);
        LocalDate toDate = fromDate.with(TemporalAdjusters.lastDayOfMonth());

        int offset = 0;
        MovementsResponse movementsResponse  = null;

        List<Transaction> allTransactions = new ArrayList<>();

        do {
            movementsResponse = ingApiClient.getApiRestProductMovements(account.getApiIdentifier(),
                    fromDate,
                    toDate,
                    offset
            );

            List<Transaction> transactions = movementsResponse
                    .getElements()
                    .stream()
                    .map(element -> toTinkTransaction(account, element))
                    .collect(Collectors.toList());

            allTransactions.addAll(transactions);

            offset += movementsResponse.getCount();

        } while (movementsResponse.getTotal() > offset);

        return PaginatorResponseImpl.create(allTransactions);
    }

    private static Transaction toTinkTransaction(Account account, Element element) {
        Transaction.Builder builder = new Transaction.Builder();

        builder.setAmount(new Amount(account.getBalance().getCurrency(), element.getAmount()))
                .setDate(LocalDate.parse(element.getEffectiveDate(), IngUtils.DATE_FORMATTER))
                .setDescription(element.getDescription())
                .setPending(false);

        return builder.build();
    }
}
