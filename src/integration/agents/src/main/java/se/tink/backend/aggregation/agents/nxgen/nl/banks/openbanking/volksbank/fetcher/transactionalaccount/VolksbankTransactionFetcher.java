package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.fetcher.transactionalaccount;

import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankUtils;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.authenticator.ConsentFetcher;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.entities.transactions.TransactionsEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class VolksbankTransactionFetcher
        implements TransactionKeyPaginator<TransactionalAccount, String> {

    private final VolksbankApiClient apiClient;
    private final ConsentFetcher consentFetcher;

    public VolksbankTransactionFetcher(
            final VolksbankApiClient apiClient, final ConsentFetcher consentFetcher) {
        this.apiClient = apiClient;
        this.consentFetcher = consentFetcher;
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            final TransactionalAccount account, final String key) {

        final String consentId = consentFetcher.fetchConsent();

        final TransactionsEntity response =
                apiClient
                        .readTransactions(account, VolksbankUtils.splitURLQuery(key), consentId)
                        .getTransactions();
        return response;
    }
}
