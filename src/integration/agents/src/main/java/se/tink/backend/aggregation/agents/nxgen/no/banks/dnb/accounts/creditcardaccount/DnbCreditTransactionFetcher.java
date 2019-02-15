package se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.accounts.creditcardaccount;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.DnbApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.DnbConstants;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.accounts.creditcardaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.accounts.creditcardaccount.entities.TransactionsEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.accounts.creditcardaccount.rpc.FetchCreditCardTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

public class DnbCreditTransactionFetcher implements TransactionFetcher<CreditCardAccount> {

    private DnbApiClient apiClient;

    public DnbCreditTransactionFetcher(DnbApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(CreditCardAccount account) {
        String transactionType = account.getFromTemporaryStorage(DnbConstants.CreditCard.TRANSACTION_TYPE);
        boolean mainCard = DnbConstants.CreditCard.MAINHOLDER.equalsIgnoreCase(transactionType);

        FetchCreditCardTransactionsResponse fetchResponse = apiClient.fetchCreditCardTransactions(account);

        return fetchResponse.getTransactions().stream()
                .map(TransactionsEntity::getTransactions)
                .flatMap(Collection::stream)
                .filter(tx -> includeTransaction(mainCard, tx))
                .map(TransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }

    private boolean includeTransaction(boolean isMainCard, TransactionEntity transaction) {
        // if main card filter out any coHolder card transactions.
        // transaction types included are ACCOUNT and MAINHOLDER
        if (isMainCard) {
            return !DnbConstants.CreditCard.COHOLDER.equalsIgnoreCase(transaction.getTransactionItemType());
            // if NOT main card filter out any non-coHolder card transactions (this is bank default behaviour)
        } else {
            return DnbConstants.CreditCard.COHOLDER.equalsIgnoreCase(transaction.getTransactionItemType());
        }
    }
}
