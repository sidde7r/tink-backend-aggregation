package se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.fetcher.creditcards;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.VolvoFinansApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.VolvoFinansConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.fetcher.creditcards.entities.CreditCardEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.fetcher.creditcards.entities.CreditCardTransactionEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import static org.apache.commons.lang3.ObjectUtils.max;

public class VolvoFinansCreditCardFetcher implements AccountFetcher<CreditCardAccount>,
        TransactionDatePaginator<CreditCardAccount> {

    private final VolvoFinansApiClient apiClient;

    public VolvoFinansCreditCardFetcher(VolvoFinansApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        return apiClient.creditCardAccounts().stream()
                .map(CreditCardEntity::toTinkAccount)
                .collect(Collectors.toList());
    }

    @Override
    public PaginatorResponse getTransactionsFor(CreditCardAccount account, Date fromDate, Date toDate) {

        LocalDate localStartDate = fromDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate localToDate = toDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        List<CreditCardTransaction> transactions = new ArrayList<>();

        /* outer loop sets time period to query for transactions */
        while (!localToDate.isBefore(localStartDate)) {
            /* set 'localFromDate' to first of month (or to 'localStartDate' if first of month is outside requested time period) */
            LocalDate localFromDate = max(localToDate.minusDays(localToDate.getDayOfMonth()-1), localStartDate);
            transactions.addAll(getTransactionsBatch(account, localFromDate, localToDate));
            localToDate = localFromDate.minusDays(1);
        }

        return PaginatorResponseImpl.create(transactions);
    }

    private List<CreditCardTransaction> getTransactionsBatch(CreditCardAccount account, LocalDate localFromDate,
            LocalDate localToDate) {
        String accountId = account.getTemporaryStorage(VolvoFinansConstants.UrlParameters.ACCOUNT_ID, String.class);
        int limit = VolvoFinansConstants.Pagination.LIMIT;
        int offset = 0;

        List<CreditCardTransaction> transactions = new ArrayList<>();

        boolean pagesLeft = true;
        while (pagesLeft) {
            List<CreditCardTransaction> collected = apiClient
                    .creditCardAccountTransactions(accountId, localFromDate, localToDate, limit, offset)
                    .stream()
                    .map(CreditCardTransactionEntity::toTinkTransaction)
                    .collect(Collectors.toList());
            transactions.addAll(collected);

            pagesLeft = !(collected.size() < limit);
            offset += limit;
        }
        return transactions;
    }
}
