package se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.fetcher.transactions;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.VolvoFinansApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.VolvoFinansConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.fetcher.transactions.entities.TransactionEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import static org.apache.commons.lang3.ObjectUtils.max;

public class VolvoFinansTransactionFetcher<A extends Account> implements TransactionDatePaginator<A> {

    private final Logger log = LoggerFactory.getLogger(VolvoFinansTransactionFetcher.class);

    private final VolvoFinansApiClient apiClient;

    public VolvoFinansTransactionFetcher(VolvoFinansApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<Transaction> getTransactionsFor(A account, Date fromDate, Date toDate) {
        String accountId = account.getTemporaryStorage(VolvoFinansConstants.UrlParameters.ACCOUNT_ID, String.class);
        int limit = 100;

        final LocalDate START_DATE = fromDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate to = toDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        List<Transaction> transactions = new ArrayList<>();

        /* outer loop sets time period to query for transactions */
        while (!to.isBefore(START_DATE)) {

            /* set 'from' to first of month (or to 'START_DATE' if first of month is outside requested time period) */
            LocalDate from = max(to.minusDays(to.getDayOfMonth()-1), START_DATE);

            /* inner loop checks for paging within the current time period */
            boolean pagesLeft = true;
            int offset = 0;
            while (pagesLeft) {
                List<Transaction> collected = Arrays
                        .stream(apiClient.creditCardAccountTransactions(accountId, from, to, limit, offset))
                        .map(TransactionEntity::toTinkTransaction)
                        .collect(Collectors.toList());

                transactions.addAll(collected);

                pagesLeft = !(collected.size() < limit);
                offset += limit;
            }

            to = from.minusDays(1);
        }

        return transactions;
    }
}
