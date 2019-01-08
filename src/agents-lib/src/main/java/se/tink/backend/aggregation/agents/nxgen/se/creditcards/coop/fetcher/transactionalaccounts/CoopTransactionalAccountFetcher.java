package se.tink.backend.aggregation.agents.nxgen.se.creditcards.coop.fetcher.transactionalaccounts;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.coop.CoopApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.coop.CoopConstants;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.coop.rpc.UserSummaryResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class CoopTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount>,
        TransactionPagePaginator<TransactionalAccount> {

    private final CoopApiClient apiClient;
    private final SessionStorage sessionStorage;

    public CoopTransactionalAccountFetcher(CoopApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;

        this.sessionStorage = sessionStorage;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        UserSummaryResponse userSummary =
                sessionStorage.get(CoopConstants.Storage.USER_SUMMARY, UserSummaryResponse.class)
                .orElseThrow(() -> new IllegalStateException("No user data in Session storage"));

        String credentialsId = sessionStorage.get(CoopConstants.Storage.CREDENTIALS_ID);
        return userSummary.toTinkAccounts(credentialsId);
    }

    @Override
    public PaginatorResponse getTransactionsFor(TransactionalAccount account, int page) {
        int accountType = Integer.parseInt(account.getBankIdentifier());

        return apiClient.fetchTransactions(page, accountType);
    }
}
