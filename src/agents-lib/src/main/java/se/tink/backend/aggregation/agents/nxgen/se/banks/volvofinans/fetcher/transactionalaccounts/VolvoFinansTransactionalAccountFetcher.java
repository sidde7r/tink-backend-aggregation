package se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.fetcher.transactionalaccounts;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.VolvoFinansApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.VolvoFinansConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.fetcher.transactionalaccounts.rpc.SavingsAccountsResponse;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.libraries.date.DateUtils;
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
        try {
            SavingsAccountsResponse savingsAccountsResponse = apiClient.savingsAccounts();
            Collection<TransactionalAccount> tinkAccounts = savingsAccountsResponse.getTinkAccounts();
            for (TransactionalAccount tinkAccount : tinkAccounts) {
                LocalDate toDate = LocalDate.now();
                LocalDate fromDate = toDate.minusDays(30);
                getTransactionsFor(tinkAccount, DateUtils.toJavaUtilDate(fromDate), DateUtils.toJavaUtilDate(toDate));
            }
        } catch (Exception e) {
            log.info(VolvoFinansConstants.LogTags.SAVINGS_ACCOUNTS +
                    " Failed to fetch and parse savings accounts", e);
        }

        return new ArrayList<>();
    }

    @Override
    public PaginatorResponse getTransactionsFor(TransactionalAccount account, Date fromDate, Date toDate) {
        try {
            String accountId = account.getBankIdentifier();
            int limit = VolvoFinansConstants.Pagination.LIMIT;

            final LocalDate localStartDate = DateUtils.toJavaTimeLocalDate(fromDate);
            LocalDate localToDate = DateUtils.toJavaTimeLocalDate(toDate);
            LocalDate localFromDate = max(localToDate.minusDays(localToDate.getDayOfMonth()-1), localStartDate);

            int offset = 0;
            log.infoExtraLong(apiClient.savingsAccountTransactions(accountId, localFromDate, localToDate, limit, offset),
                    LogTag.from(VolvoFinansConstants.LogTags.SAVINGS_ACCOUNT_TRANSACTIONS));

        } catch (Exception e) {
            log.info(VolvoFinansConstants.LogTags.SAVINGS_ACCOUNT_TRANSACTIONS +
                    " Failed to fetch savings account transactions", e);
        }

        return PaginatorResponseImpl.createEmpty();
    }
}
