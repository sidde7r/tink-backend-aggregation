package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.temporal.TemporalAdjusters;
import java.util.Collection;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.BankiaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.BankiaConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional.entities.AccountEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

public class BankiaTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount>,
        TransactionPagePaginator<TransactionalAccount> {

    private static final Logger log = LoggerFactory.getLogger(BankiaTransactionalAccountFetcher.class);
    private final BankiaApiClient apiClient;
    private final LocalDate nowInLocalDate;

    public BankiaTransactionalAccountFetcher(BankiaApiClient apiClient) {
        this.apiClient = apiClient;
        this.nowInLocalDate = LocalDate.now(BankiaConstants.ZONE_ID);
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return apiClient.getAccounts()
                .stream()
                .filter(account-> {
                    // Temporary logging of unknown types.
                    // Todo: change this to `.filter(AccountEntity::isAccountTypeTransactional)`

                    if (account.isAccountTypeTransactional()) {
                        return true;
                    }

                    log.info("{} Unknown account type: {}", BankiaConstants.Logging.UNKNOWN_ACCOUNT_TYPE.toString(),
                            account.getBankiaAccountType());
                    return false;
                })
                .map(AccountEntity::toTinkAccount)
                .collect(Collectors.toList());
    }

    @Override
    public PaginatorResponse getTransactionsFor(TransactionalAccount account, int page) {
        // NOTE: this code was taken from the TransactionMonthPaginationController in order to leverage
        // the canFetchMore() functionality.
        // Todo: refactor this when the `TransactionMonthPaginationController` can handle canFetchMore.
        LocalDate dateToFetch = nowInLocalDate.minusMonths(page);

        Year year = Year.from(dateToFetch);
        Month month = Month.from(dateToFetch);

        // First day of the month
        LocalDate fromDate = LocalDate.of(year.getValue(), month, 1);
        // Last day of the month
        LocalDate toDate = fromDate.with(TemporalAdjusters.lastDayOfMonth());

        return apiClient.getTransactions(account, fromDate, toDate);
    }
}
