package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.fetcher.transactionalaccount;

import com.google.common.base.Strings;
import java.time.LocalDate;
import javax.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusConstants.StorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.KeyWithInitiDateFromFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@Slf4j
@RequiredArgsConstructor
public final class BelfiusTransactionFetcher
        implements KeyWithInitiDateFromFetcher<TransactionalAccount, String> {

    private static final LocalDate START_DATE_ALL_HISTORY = LocalDate.ofEpochDay(0);
    private static final String MAX_PAGE_SIZE = "400";

    private final BelfiusApiClient apiClient;
    private final PersistentStorage persistentStorage;

    private OAuth2Token getOauth2Token() {
        return persistentStorage
                .get(StorageKeys.OAUTH_TOKEN, OAuth2Token.class)
                .orElseThrow(
                        () -> new IllegalStateException(SessionError.SESSION_EXPIRED.exception()));
    }

    @Override
    public TransactionKeyPaginatorResponse<String> fetchTransactionsFor(
            TransactionalAccount account, LocalDate dateFrom) {

        return generalFetchTransactions(account, null);
    }

    @Override
    public LocalDate minimalFromDate() {
        return START_DATE_ALL_HISTORY;
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            TransactionalAccount account, @Nullable String key) {

        return generalFetchTransactions(account, key);
    }

    private TransactionKeyPaginatorResponse<String> generalFetchTransactions(
            TransactionalAccount account, @Nullable String key) {
        log.info("Is next page param present: {} ", !Strings.isNullOrEmpty(key));

        if (persistentStorage.containsKey(StorageKeys.SCA_TOKEN)) {
            String scaToken = persistentStorage.get(StorageKeys.SCA_TOKEN);
            return apiClient.fetchTransactionsFromDate(
                    getOauth2Token(),
                    key,
                    account.getApiIdentifier(),
                    scaToken,
                    minimalFromDate(),
                    MAX_PAGE_SIZE);
        }
        return apiClient.fetchTransactionsFromLast90Days(
                getOauth2Token(), key, account.getApiIdentifier());
    }
}
