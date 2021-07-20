package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.urlfactory;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
public class PolishPostTransactionsApiUrlFactory implements PolishTransactionsApiUrlFactory {

    private final URL baseUrl;
    private final String version;

    private URL getBaseAccountsUrl() {
        return baseUrl.concatWithSeparator(version)
                .concatWithSeparator("accounts")
                .concatWithSeparator(version);
    }

    @Override
    public URL getTransactionsUrl(String accountNumber) {
        throw new UnsupportedOperationException("This method should be used only in Get Client");
    }

    @Override
    public URL getTransactionsContinuationUrl(String path) {
        throw new UnsupportedOperationException("This method should be used only in Get Client");
    }

    public URL getTransactionsUrl(
            PolishApiConstants.Transactions.TransactionTypeRequest transactionTypeRequest) {
        return getBaseAccountsUrl()
                .concatWithSeparator(transactionTypeRequest.getPostTransactionsEndpoint());
    }
}
