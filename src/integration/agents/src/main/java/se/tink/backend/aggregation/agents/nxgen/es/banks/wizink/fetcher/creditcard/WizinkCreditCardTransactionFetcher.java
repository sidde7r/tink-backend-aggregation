package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.creditcard;

import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.WizinkApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.WizinkConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.WizinkStorage;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.creditcard.entities.Movements;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

public class WizinkCreditCardTransactionFetcher implements TransactionFetcher<CreditCardAccount> {

    private WizinkApiClient wizinkApiClient;
    private WizinkStorage wizinkStorage;

    public WizinkCreditCardTransactionFetcher(
            WizinkApiClient wizinkApiClient, WizinkStorage wizinkStorage) {
        this.wizinkApiClient = wizinkApiClient;
        this.wizinkStorage = wizinkStorage;
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(CreditCardAccount account) {
        String encodedAccountNumber =
                account.getFromTemporaryStorage(StorageKeys.ENCODED_ACCOUNT_NUMBER);

        Movements movements =
                wizinkApiClient
                        .fetchCreditCardTransactionsFrom90Days(encodedAccountNumber)
                        .getMovements();

        List<AggregationTransaction> transactions = movements.getTransactions();
        transactions.addAll(
                fetchTransactionsOlderThan90DaysIfPresent(
                        encodedAccountNumber, movements.canFetchTransactionsOlderThan90Days()));

        return transactions;
    }

    private List<AggregationTransaction> fetchTransactionsOlderThan90DaysIfPresent(
            String accountNumber, boolean transactionsFrom90Days) {
        boolean firstFullRefresh = wizinkStorage.getFirstFullRefreshFlag();
        if (firstFullRefresh && transactionsFrom90Days) {
            return getTransactionsFromMoreThan90Days(accountNumber, getSessionId(accountNumber));
        }
        return Collections.emptyList();
    }

    private String getSessionId(String accountNumber) {
        return wizinkApiClient
                .prepareOtpRequestToUserMobilePhone(accountNumber)
                .getMovements()
                .getOtp()
                .getBharosaSessionId();
    }

    private List<AggregationTransaction> getTransactionsFromMoreThan90Days(
            String accountNumber, String sessionId) {
        return wizinkApiClient
                .fetchCreditCardTransactionsOlderThan90Days(accountNumber, sessionId)
                .getMovements()
                .getTransactions();
    }
}
