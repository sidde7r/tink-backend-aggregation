package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transaction;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.LaBanquePostaleApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.LaBanquePostaleConstants.Urls;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.Account;

@RequiredArgsConstructor
public class LaBanquePostaleTransactionFetcher<T extends Account>
        implements TransactionKeyPaginator<T, String> {

    private final LaBanquePostaleApiClient laBanquePostaleApiClient;

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(T account, String nextUrl) {
        String url =
                Strings.isNullOrEmpty(nextUrl)
                        ? String.format(Urls.FETCH_TRANSACTIONS, account.getApiIdentifier())
                        : nextUrl;
        return laBanquePostaleApiClient.fetchTransactionsLaBanquePostal(url);
    }
}
