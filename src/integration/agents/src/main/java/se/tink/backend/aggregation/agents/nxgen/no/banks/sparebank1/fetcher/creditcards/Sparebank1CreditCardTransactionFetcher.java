package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.creditcards;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1ApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.creditcards.rpc.CreditCardTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class Sparebank1CreditCardTransactionFetcher
        implements TransactionKeyPaginator<CreditCardAccount, String> {
    private final Sparebank1ApiClient apiClient;

    public Sparebank1CreditCardTransactionFetcher(Sparebank1ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public CreditCardTransactionsResponse getTransactionsFor(
            CreditCardAccount account, String key) {
        // The key is the url for fetching more transactions, which is provided in the response from
        // the bank
        // as long as there are more transactions to fetch.
        if (Strings.isNullOrEmpty(key)) {
            return apiClient.fetchCreditCardTransactions(account.getApiIdentifier());
        }

        return apiClient.get(key, CreditCardTransactionsResponse.class);
    }
}
