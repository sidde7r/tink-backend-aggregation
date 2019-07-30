package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.fetcher.transactionalaccount;

import java.util.NoSuchElementException;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankUtils;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.authenticator.ConsentFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class VolksbankTransactionFetcher
        implements TransactionKeyPaginator<TransactionalAccount, String> {

    private final VolksbankApiClient apiClient;
    private final ConsentFetcher consentFetcher;
    private final PersistentStorage persistentStorage;

    public VolksbankTransactionFetcher(
            final VolksbankApiClient apiClient,
            final ConsentFetcher consentFetcher,
            final PersistentStorage persistentStorage) {
        this.apiClient = apiClient;
        this.consentFetcher = consentFetcher;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            final TransactionalAccount account, final String key) {

        final String consentId = consentFetcher.fetchConsent();

        final OAuth2Token oauthToken =
                persistentStorage
                        .get(Storage.OAUTH_TOKEN, OAuth2Token.class)
                        .orElseThrow(() -> new NoSuchElementException("Missing Oauth token!"));

        return apiClient
                .readTransactions(account, VolksbankUtils.splitURLQuery(key), consentId, oauthToken)
                .getTransactions();
    }
}
