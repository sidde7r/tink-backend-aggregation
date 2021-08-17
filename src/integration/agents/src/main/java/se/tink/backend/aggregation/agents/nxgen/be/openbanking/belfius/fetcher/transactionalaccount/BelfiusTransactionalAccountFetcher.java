package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.fetcher.transactionalaccount;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusConstants.StorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.KeyWithInitiDateFromFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@Slf4j
@RequiredArgsConstructor
public class BelfiusTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                KeyWithInitiDateFromFetcher<TransactionalAccount, String> {

    private static final LocalDate START_DATE_ALL_HISTORY = LocalDate.ofEpochDay(0);

    private final BelfiusApiClient apiClient;
    private final PersistentStorage persistentStorage;

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        final String logicalId = persistentStorage.get(StorageKeys.LOGICAL_ID);

        return apiClient
                .fetchAccountById(getOauth2Token(), logicalId)
                .toTinkAccount(logicalId)
                .map(Collections::singletonList)
                .orElse(Collections.emptyList());
    }

    private OAuth2Token getOauth2Token() {
        return persistentStorage
                .get(StorageKeys.OAUTH_TOKEN, OAuth2Token.class)
                .orElseThrow(
                        () -> new IllegalStateException(SessionError.SESSION_EXPIRED.exception()));
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            TransactionalAccount account, String key) {
        log.info("Is next page param present: {} ", !Strings.isNullOrEmpty(key));
        return apiClient.fetchTransactionsForAccount(
                getOauth2Token(), key, account.getApiIdentifier());
    }

    @Override
    public TransactionKeyPaginatorResponse<String> fetchTransactionsFor(
            TransactionalAccount account, LocalDate dateFrom) {
        final String scaToken = persistentStorage.get(StorageKeys.SCA_TOKEN);
        return apiClient.fetchTransactionsForAccount(
                getOauth2Token(), account.getApiIdentifier(), scaToken, dateFrom);
    }

    @Override
    public LocalDate minimalFromDate() {
        return START_DATE_ALL_HISTORY;
    }
}
