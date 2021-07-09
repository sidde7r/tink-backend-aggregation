package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.ConsentController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.rpc.BaseTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.UpcomingTransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;

public class RedsysUpcomingTransactionFetcher
        implements UpcomingTransactionFetcher<TransactionalAccount> {
    private final RedsysApiClient apiClient;
    private final ConsentController consentController;

    public RedsysUpcomingTransactionFetcher(
            RedsysApiClient apiClient, ConsentController consentController) {
        this.apiClient = apiClient;
        this.consentController = consentController;
    }

    @Override
    public Collection<UpcomingTransaction> fetchUpcomingTransactionsFor(
            TransactionalAccount account) {
        final BaseTransactionsResponse response =
                apiClient.fetchPendingTransactions(
                        account.getApiIdentifier(), consentController.getConsentId());
        return response.getUpcomingTransactions();
    }
}
