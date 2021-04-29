package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.creditcard;

import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.WizinkApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.WizinkConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.WizinkStorage;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.creditcard.entities.CardTransactionsResponse;
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
        boolean firstFullRefresh = wizinkStorage.getFirstFullRefreshFlag();

        CardTransactionsResponse cardTransactionsResponse =
                wizinkApiClient
                        .fetchCreditCardTransactionsFrom90Days(encodedAccountNumber)
                        .getCardTransactionsResponse();
        List<AggregationTransaction> transactions = cardTransactionsResponse.getTransactions();
        Optional<String> sessionId = getSessionId(encodedAccountNumber);
        if (firstFullRefresh
                && cardTransactionsResponse.canFetchTransactionsOlderThan90Days()
                && sessionId.isPresent()) {
            transactions.addAll(
                    getTransactionsFromMoreThan90Days(encodedAccountNumber, sessionId.get()));
        }
        return transactions;
    }

    private List<AggregationTransaction> getTransactionsFromMoreThan90Days(
            String accountNumber, String sessionId) {
        return wizinkApiClient
                .fetchCreditCardTransactionsOlderThan90Days(accountNumber, sessionId)
                .getCardTransactionsResponse()
                .getTransactions();
    }

    private Optional<String> getSessionId(String accountNumber) {
        return wizinkApiClient
                .fetchSessionIdForOlderCardTransactions(accountNumber)
                .getCardTransactionsResponse()
                .getSessionEntity()
                .getSessionId();
    }
}
