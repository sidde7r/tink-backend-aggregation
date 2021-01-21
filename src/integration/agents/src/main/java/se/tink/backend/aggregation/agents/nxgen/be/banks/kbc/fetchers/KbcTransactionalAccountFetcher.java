package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.KbcApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.KbcConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto.AgreementDto;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto.FutureTransactionsResponse;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.UpcomingTransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class KbcTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionKeyPaginator<TransactionalAccount, String>,
                UpcomingTransactionFetcher<TransactionalAccount> {

    private final KbcApiClient apiClient;
    private String userLanguage;
    private final SessionStorage sessionStorage;

    public KbcTransactionalAccountFetcher(
            KbcApiClient apiClient, String userLanguage, final SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.userLanguage = userLanguage;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        final byte[] cipherKey =
                EncodingUtils.decodeBase64String(
                        sessionStorage.get(KbcConstants.Encryption.AES_SESSION_KEY_KEY));

        return apiClient.fetchAccounts(userLanguage, cipherKey).getAgreements().stream()
                .filter(
                        agreement ->
                                agreement.getAccountType().isPresent()
                                        && AccountTypes.SAVINGS
                                                == agreement.getAccountType().orElse(null))
                .map(AgreementDto::toTransactionalAccount)
                .collect(Collectors.toList());
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            TransactionalAccount account, String key) {
        final byte[] cipherKey =
                EncodingUtils.decodeBase64String(
                        sessionStorage.get(KbcConstants.Encryption.AES_SESSION_KEY_KEY));

        try {
            return apiClient.fetchTransactions(
                    account.getApiIdentifier(), key, userLanguage, cipherKey);
        } catch (IllegalStateException e) {
            if (noTransactionsFoundForLast12Months(e)) {
                return new TransactionKeyPaginatorResponseImpl<>();
            }
            throw e;
        }
    }

    private boolean noTransactionsFoundForLast12Months(IllegalStateException e) {
        return e.getMessage() != null
                && (e.getMessage()
                                .toLowerCase()
                                .contains(KbcConstants.ErrorMessage.NO_TRANSACTIONS_FOUND)
                        || e.getMessage()
                                .toLowerCase()
                                .contains(KbcConstants.ErrorMessage.NO_TRANSACTIONS_FOUND_NL)
                        || e.getMessage()
                                .toLowerCase()
                                .contains(KbcConstants.ErrorMessage.NO_TRANSACTIONS_FOUND_FR)
                        || e.getMessage()
                                .toLowerCase()
                                .contains(KbcConstants.ErrorMessage.NO_TRANSACTIONS_FOUND_DE));
    }

    @Override
    public Collection<UpcomingTransaction> fetchUpcomingTransactionsFor(
            TransactionalAccount account) {
        Collection<UpcomingTransaction> upcomingTransactions = Lists.newArrayList();

        final byte[] cipherKey =
                EncodingUtils.decodeBase64String(
                        sessionStorage.get(KbcConstants.Encryption.AES_SESSION_KEY_KEY));

        String key = null;
        do {
            FutureTransactionsResponse response =
                    apiClient.fetchFutureTransactions(account.getApiIdentifier(), key, cipherKey);
            upcomingTransactions.addAll(response.getUpcomingTransactions());
            key = response.hasNext() ? response.nextKey() : null;
        } while (key != null);

        return upcomingTransactions;
    }
}
