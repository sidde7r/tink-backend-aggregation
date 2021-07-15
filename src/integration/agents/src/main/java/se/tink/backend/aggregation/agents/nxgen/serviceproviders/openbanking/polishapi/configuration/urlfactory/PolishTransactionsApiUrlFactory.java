package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.urlfactory;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public interface PolishTransactionsApiUrlFactory {
    URL getTransactionsUrl(String accountNumber);

    URL getTransactionsContinuationUrl(String path);

    URL getTransactionsUrl(
            PolishApiConstants.Transactions.TransactionTypeRequest transactionTypeRequest);
}
