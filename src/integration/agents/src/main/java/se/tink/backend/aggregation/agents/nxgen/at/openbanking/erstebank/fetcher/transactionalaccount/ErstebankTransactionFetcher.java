package se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.fetcher.transactionalaccount;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.ErstebankApiClient;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.ErstebankConstants.EndPoints;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.BerlinGroupTransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class ErstebankTransactionFetcher extends BerlinGroupTransactionFetcher {

    private ErstebankApiClient erstebankApiClient;

    public ErstebankTransactionFetcher(ErstebankApiClient apiClient) {
        super(apiClient);
        this.erstebankApiClient = apiClient;
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            TransactionalAccount account, String nextUrl) {
        final String url =
                Strings.isNullOrEmpty(nextUrl)
                        ? String.format(EndPoints.TRANSACTIONS, account.getApiIdentifier())
                        : EndPoints.BASIC + nextUrl;
        return erstebankApiClient.fetchTransactionsForErste(url);
    }
}
