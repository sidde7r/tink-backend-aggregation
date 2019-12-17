package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.transactionalaccount;

import static java.time.temporal.ChronoUnit.DAYS;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import java.time.LocalDate;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsUserState;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.initialtransactions.InitialTransactionsFromCertainDateFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class SibsTransactionalAccountTransactionFetcher
        implements TransactionKeyPaginator<TransactionalAccount, String>,
                InitialTransactionsFromCertainDateFetcher<
                        TransactionalAccount, TransactionKeyPaginatorResponse<String>> {
    private static final Logger LOG =
            LoggerFactory.getLogger(SibsTransactionalAccountTransactionFetcher.class);
    private static final int MAX_DAYS_OF_TRANSACTION_HISTORY = 89;
    private static final String ENCODED_SPACE = "%20";

    private final SibsBaseApiClient apiClient;
    private final SibsUserState userState;

    public SibsTransactionalAccountTransactionFetcher(
            SibsBaseApiClient apiClient, SibsUserState userState) {
        this.apiClient = apiClient;
        this.userState = userState;
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            TransactionalAccount account, String key) {
        return apiClient.getTransactionsForKey(
                Preconditions.checkNotNull(key).replaceAll(StringUtils.SPACE, ENCODED_SPACE));
    }

    @Override
    public TransactionKeyPaginatorResponse<String> fetchInitialTransactionsFor(
            TransactionalAccount account, LocalDate fromDate) {
        if (!canFetchTransactionsFrom(fromDate)) {
            LOG.warn(
                    String.format(
                            "Unable to fetch transactions until certainDate(%s days ago), fetching transactions 90 days backwards",
                            DAYS.between(fromDate, LocalDate.now())));
            fromDate = getOldestAllowedFromDate();
        }

        return apiClient.getAccountTransactions(account, fromDate);
    }

    private boolean canFetchTransactionsFrom(LocalDate fromDate) {
        return DAYS.between(fromDate, LocalDate.now()) <= MAX_DAYS_OF_TRANSACTION_HISTORY
                || !userState.getConsent().isConsentOlderThan30Minutes();
    }

    @VisibleForTesting
    static LocalDate getOldestAllowedFromDate() {
        return LocalDate.now().minusDays(MAX_DAYS_OF_TRANSACTION_HISTORY);
    }
}
