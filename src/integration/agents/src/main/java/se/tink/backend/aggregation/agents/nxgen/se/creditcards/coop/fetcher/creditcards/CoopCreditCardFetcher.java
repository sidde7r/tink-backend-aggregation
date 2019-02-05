package se.tink.backend.aggregation.agents.nxgen.se.creditcards.coop.fetcher.creditcards;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.coop.CoopApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.coop.CoopConstants;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.coop.rpc.UserSummaryResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class CoopCreditCardFetcher implements AccountFetcher<CreditCardAccount>,
        TransactionPagePaginator<CreditCardAccount> {

    private final CoopApiClient apiClient;
    private final SessionStorage sessionStorage;

    public CoopCreditCardFetcher(CoopApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        UserSummaryResponse userSummary =
                sessionStorage.get(CoopConstants.Storage.USER_SUMMARY, UserSummaryResponse.class)
                .orElseThrow(() -> new IllegalStateException("No user data in Session storage"));

        String credentialsId = sessionStorage.get(CoopConstants.Storage.CREDENTIALS_ID);
        return userSummary.toTinkCards(credentialsId);
    }

    @Override
    public PaginatorResponse getTransactionsFor(CreditCardAccount account, int page) {
        int accountType = Integer.parseInt(account.getBankIdentifier());

        return apiClient.fetchTransactions(page, accountType);
    }
}
