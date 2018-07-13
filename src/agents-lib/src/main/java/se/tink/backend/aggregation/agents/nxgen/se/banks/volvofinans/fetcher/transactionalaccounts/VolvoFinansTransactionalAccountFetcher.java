package se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.fetcher.transactionalaccounts;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.VolvoFinansApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.VolvoFinansConstants;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import static org.apache.commons.lang3.ObjectUtils.max;

public class VolvoFinansTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount>,
        TransactionDatePaginator<TransactionalAccount> {

    private final AggregationLogger log = new AggregationLogger(VolvoFinansApiClient.class);
    private final VolvoFinansApiClient apiClient;

    public VolvoFinansTransactionalAccountFetcher(VolvoFinansApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        log.infoExtraLong(apiClient.savingsAccounts(), LogTag.from(VolvoFinansConstants.LogTags.SAVINGS_ACCOUNTS));
        return new ArrayList<>();
    }

    @Override
    public Collection<Transaction> getTransactionsFor(TransactionalAccount account, Date fromDate, Date toDate) {
        String accountId = account.getTemporaryStorage(VolvoFinansConstants.UrlParameters.ACCOUNT_ID, String.class);
        int limit = VolvoFinansConstants.Pagination.LIMIT;

        final LocalDate localStartDate = fromDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate localToDate = toDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate localFromDate = max(localToDate.minusDays(localToDate.getDayOfMonth()-1), localStartDate);

        int offset = 0;
        log.infoExtraLong(apiClient.savingsAccountTransactions(accountId, localFromDate, localToDate, limit, offset),
                LogTag.from(VolvoFinansConstants.LogTags.SAVINGS_ACCOUNT_TRANSACTIONS));
        return new ArrayList<>();
    }
}
