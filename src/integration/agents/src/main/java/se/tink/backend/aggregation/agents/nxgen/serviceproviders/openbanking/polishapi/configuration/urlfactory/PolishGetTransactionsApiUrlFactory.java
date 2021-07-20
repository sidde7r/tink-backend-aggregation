package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.urlfactory;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
public class PolishGetTransactionsApiUrlFactory implements PolishTransactionsApiUrlFactory {

    private final URL baseUrl;
    private final String apiType;
    private final String version;

    private URL getBaseAccountsUrl() {
        return baseUrl.concatWithSeparator(apiType)
                .concatWithSeparator(version)
                .concatWithSeparator("accounts");
    }

    @Override
    public URL getTransactionsUrl(String accountNumber) {
        return getBaseAccountsUrl()
                .concatWithSeparator(accountNumber)
                .concatWithSeparator("transactions");
    }

    @Override
    public URL getTransactionsContinuationUrl(String path) {
        return baseUrl.concat(path);
    }

    @Override
    public URL getTransactionsUrl(
            PolishApiConstants.Transactions.TransactionTypeRequest transactionTypeRequest) {
        throw new UnsupportedOperationException("This method should be used only in Post Client");
    }
}
