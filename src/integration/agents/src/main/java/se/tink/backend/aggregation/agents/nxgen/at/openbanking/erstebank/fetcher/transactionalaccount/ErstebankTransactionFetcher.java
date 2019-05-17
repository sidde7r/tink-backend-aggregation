package se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.fetcher.transactionalaccount;

import se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.ErstebankApiClient;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.ErstebankConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.BerlinGroupTransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class ErstebankTransactionFetcher extends BerlinGroupTransactionFetcher {

    public ErstebankTransactionFetcher(ErstebankApiClient apiClient) {
        super(apiClient);
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            TransactionalAccount account, String key) {
        return apiClient.fetchTransactions(
                String.format(Urls.TRANSACTIONS, account.getApiIdentifier()));
    }
}
