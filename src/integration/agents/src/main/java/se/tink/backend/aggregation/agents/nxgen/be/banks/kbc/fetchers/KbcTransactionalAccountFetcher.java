package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers;

import com.google.common.collect.Lists;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.KbcApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.KbcConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto.AgreementDto;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto.FutureTransactionsResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.UpcomingTransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

public class KbcTransactionalAccountFetcher  implements AccountFetcher<TransactionalAccount>,
        TransactionKeyPaginator<TransactionalAccount, String>, UpcomingTransactionFetcher<TransactionalAccount> {

    private static final AggregationLogger LOGGER = new AggregationLogger(KbcTransactionalAccountFetcher.class);
    private static final Set<AccountTypes> SAVINGS_OR_CHECKING =
            Collections.unmodifiableSet(EnumSet.of(AccountTypes.CHECKING,
                    AccountTypes.SAVINGS));

    private final KbcApiClient apiClient;
    private String userLanguage;

    public KbcTransactionalAccountFetcher(KbcApiClient apiClient, String userLanguage) {
        this.apiClient = apiClient;
        this.userLanguage = userLanguage;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return apiClient.fetchAccounts(userLanguage).getAgreements().stream()
                .filter(
                        agreement ->
                                agreement.getAccountType().isPresent()
                                        && SAVINGS_OR_CHECKING.contains(agreement
                                        .getAccountType()
                                        .get()))
                .map(AgreementDto::toTransactionalAccount)
                .collect(Collectors.toList());
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(TransactionalAccount account, String key) {
        try {
            return apiClient.fetchTransactions(account.getBankIdentifier(), key, userLanguage);
        } catch (IllegalStateException e) {
            if (noTransactionsFoundForLast12Months(e)) {
                return new TransactionKeyPaginatorResponseImpl<>();
            }
            // Will not throw e, to be able to find error codes throw e;
            LOGGER.warnExtraLong(
                    String.format("Language: %s Error message:%s",
                            userLanguage,
                            e.getMessage()),
                    KbcConstants.LogTags.ERROR_CODE_MESSAGE);
            return new TransactionKeyPaginatorResponseImpl<>();
        }
    }

    private boolean noTransactionsFoundForLast12Months(IllegalStateException e) {
        return e.getMessage() != null &&
                e.getMessage().toLowerCase().contains(KbcConstants.ErrorMessage.NO_TRANSACTIONS_FOUND);
    }

    @Override
    public Collection<UpcomingTransaction> fetchUpcomingTransactionsFor(TransactionalAccount account) {
        Collection<UpcomingTransaction> upcomingTransactions = Lists.newArrayList();

        String key = null;
        do {
            FutureTransactionsResponse response = apiClient.fetchFutureTransactions(account.getBankIdentifier(), key);
            upcomingTransactions.addAll(response.getUpcomingTransactions());
            key = response.hasNext() ? response.nextKey() : null;
        } while (key != null);

        return upcomingTransactions;
    }
}
