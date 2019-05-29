package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.fetcher.transactionalaccount;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.SebApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.SebConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.fetcher.transactionalaccount.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class SebTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionPagePaginator<TransactionalAccount> {

    private final SebApiClient apiClient;

    // accountID -> [<URL to get first page of transactions>, <URL to get the second page of
    // transactions>, ...]
    private final Map<String, List<String>> paginationURLs;

    public SebTransactionalAccountFetcher(SebApiClient apiClient) {
        this.apiClient = apiClient;
        this.paginationURLs = new HashMap<>();
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return apiClient.fetchAccounts().toTinkAccounts();
    }

    @Override
    public PaginatorResponse getTransactionsFor(TransactionalAccount account, int page) {

        /*
        TODO: Check the pagination logic when appropriate users
        (i.e users with enough transactions) in SEB Sandbox will be available.

         I'm not exactly sure which pagination logic SEB is implementing as I haven't seen a
         response with a pagination link yet so I implemented my own logic which should be generic
         enough. After I see the pagination link pattern, I will pick correct interface instead of
         implementing my own logic to prevent code duplication. Currently, I believe that SEB follows
         the pattern which is handled by TransactionKeyPaginator
        */

        String accountID = account.getAccountNumber();

        if (!paginationURLs.containsKey(accountID))
            paginationURLs.put(accountID, new ArrayList<>());

        PaginatorResponse response;

        if (page == 0) response = apiClient.fetchTransactions(account);
        else {
            String url = paginationURLs.get(accountID).get(page - 1);

            url = SebConstants.Urls.TRANSACTIONS_NEXT_PAGE_URL_PREFIX + url;
            response = apiClient.fetchTransactions(url, false);
        }

        FetchTransactionsResponse r = (FetchTransactionsResponse) response;
        paginationURLs.get(accountID).add(r.getNextURL());
        return response;
    }
}
