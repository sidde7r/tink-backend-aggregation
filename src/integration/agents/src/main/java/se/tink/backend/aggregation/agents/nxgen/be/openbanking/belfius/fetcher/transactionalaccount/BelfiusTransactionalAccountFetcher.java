package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusConstants.StorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class BelfiusTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionDatePaginator<TransactionalAccount> {

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

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {

        final String logicalId = persistentStorage.get(StorageKeys.LOGICAL_ID);

        try {
            return PaginatorResponseImpl.create(
                    apiClient
                            .fetchTransactionsForAccount(
                                    fromDate, toDate, getOauth2Token(), logicalId)
                            .toTinkTransactions());
        } catch (HttpResponseException e) {
            if (ErrorMessages.TRANSACTION_ERROR_CODES.contains(e.getResponse().getStatus())) {
                return PaginatorResponseImpl.createEmpty(false);
            } else {
                throw e;
            }
        }
    }

    private OAuth2Token getOauth2Token() {
        return persistentStorage
                .get(StorageKeys.OAUTH_TOKEN, OAuth2Token.class)
                .orElseThrow(
                        () -> new IllegalStateException(SessionError.SESSION_EXPIRED.exception()));
    }
}
