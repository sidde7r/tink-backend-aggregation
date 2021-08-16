package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.creditcard;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.WizinkApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.WizinkConstants.StorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

public class WizinkCreditCardTransactionFetcher implements TransactionFetcher<CreditCardAccount> {

    private WizinkApiClient wizinkApiClient;

    public WizinkCreditCardTransactionFetcher(WizinkApiClient wizinkApiClient) {
        this.wizinkApiClient = wizinkApiClient;
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(CreditCardAccount account) {
        String encodedAccountNumber =
                account.getFromTemporaryStorage(StorageKeys.ENCODED_ACCOUNT_NUMBER);
        return wizinkApiClient
                .fetchCreditCardTransactionsFrom90Days(encodedAccountNumber)
                .getCardTransactionsResponse()
                .getTransactions(account);
    }
}
