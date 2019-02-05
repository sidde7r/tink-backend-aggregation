package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.BankiaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.BankiaConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional.entities.AccountEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.libraries.date.DateUtils;

public class BankiaTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount>,
        TransactionDatePaginator<TransactionalAccount> {

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

    private static Date getOneYearOldDate() {
        return DateUtils.addMonths(new Date(), -12);
    }

    @Override
    public PaginatorResponse getTransactionsFor(TransactionalAccount account, Date fromDate, Date toDate) {
        try {
            return apiClient.getTransactions(account, fromDate, toDate);
        } catch (HttpResponseException hre) {
            if (hre.getResponse().getStatus() == HttpStatus.SC_INTERNAL_SERVER_ERROR &&
                    toDate.before(getOneYearOldDate())) {
                // We will get status code 500 if we try to fetch too far back in time. If this happens we
                // indicate to the paginator that we cannot fetch more.
                return PaginatorResponseImpl.createEmpty(false);
            }

            // Re-throw unknown exception.
            throw hre;
        }
    }
}
