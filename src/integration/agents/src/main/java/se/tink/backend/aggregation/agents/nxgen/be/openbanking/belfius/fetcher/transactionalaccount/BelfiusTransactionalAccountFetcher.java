package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.fetcher.transactionalaccount;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
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
public class BelfiusTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                KeyWithInitiDateFromFetcher<TransactionalAccount, String> {

    private static final LocalDate START_DATE_ALL_HISTORY = LocalDate.ofEpochDay(0);

    private final BelfiusApiClient apiClient;
    private final PersistentStorage persistentStorage;

    public BelfiusTransactionalAccountFetcher(
            BelfiusApiClient apiClient, PersistentStorage persistentStorage) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        // TODO Belfius has no way of fetching all accounts, currently we have to hardcode the IDs,
        // and currently only ID=1 works
        List<String> accounts = Collections.singletonList("1");

        final String apiIdentifier = persistentStorage.get(StorageKeys.LOGICAL_ID);

        return accounts.stream()
                .map(
                        logicalId ->
                                apiClient
                                        .fetchAccountById(getOauth2Token(), apiIdentifier)
                                        .toTinkAccount(logicalId))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
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
        final String logicalId = persistentStorage.get(StorageKeys.LOGICAL_ID);
        return apiClient.fetchTransactionsForAccount(getOauth2Token(), key, logicalId);
    }

    @Override
    public TransactionKeyPaginatorResponse<String> fetchTransactionsFor(
            TransactionalAccount account, LocalDate dateFrom) {
        final String logicalId = persistentStorage.get(StorageKeys.LOGICAL_ID);
        final String scaToken = persistentStorage.get(StorageKeys.SCA_TOKEN);
        return apiClient.fetchTransactionsForAccount(
                getOauth2Token(), logicalId, scaToken, dateFrom);
    }

    @Override
    public LocalDate minimalFromDate() {
        return START_DATE_ALL_HISTORY;
    }
}
