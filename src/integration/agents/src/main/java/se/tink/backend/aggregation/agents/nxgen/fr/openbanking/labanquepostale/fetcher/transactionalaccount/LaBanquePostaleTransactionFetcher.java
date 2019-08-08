package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount;

import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.IngConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.LaBanquePostaleApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.BerlinGroupTransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class LaBanquePostaleTransactionFetcher extends BerlinGroupTransactionFetcher {

    private LaBanquePostaleApiClient laBanquePostaleApiClient;

    public LaBanquePostaleTransactionFetcher(LaBanquePostaleApiClient apiClient) {
        super(apiClient);
        this.laBanquePostaleApiClient = apiClient;
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            TransactionalAccount account, String nextUrl) {
        String url =
                Strings.isNullOrEmpty(nextUrl)
                        ? account.getFromTemporaryStorage(StorageKeys.TRANSACTIONS_URL)
                        : nextUrl;
        return laBanquePostaleApiClient.fetchTransactionsLaBanquePostal(url);
    }
}
