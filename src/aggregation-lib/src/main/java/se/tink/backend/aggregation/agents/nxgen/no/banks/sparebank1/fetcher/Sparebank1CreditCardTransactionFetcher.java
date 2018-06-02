package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1ApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Constants;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.rpc.CreditCardTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.http.URL;

public class Sparebank1CreditCardTransactionFetcher implements TransactionKeyPaginator<CreditCardAccount, String> {
    private final Sparebank1ApiClient apiClient;
    private final String bankName;

    public Sparebank1CreditCardTransactionFetcher(Sparebank1ApiClient apiClient, String bankKey) {
        this.apiClient = apiClient;
        this.bankName = bankKey.substring(4);
    }

    @Override
    public CreditCardTransactionsResponse getTransactionsFor(CreditCardAccount account, String key) {
        // The key is the url for fetching more transactions, which is provided in the response from the bank
        // as long as there are more transactions to fetch.
        if (Strings.isNullOrEmpty(key)) {
            URL url = Sparebank1Constants.Urls.CREDITCARD_TRANSACTIONS
                    .parameter(Sparebank1Constants.UrlParameter.BANK_NAME, bankName)
                    .parameter(Sparebank1Constants.UrlParameter.ACCOUNT_ID, account.getBankIdentifier());
            return apiClient.get(url, CreditCardTransactionsResponse.class);
        } else {
            return apiClient.get(key, CreditCardTransactionsResponse.class);
        }
    }
}
