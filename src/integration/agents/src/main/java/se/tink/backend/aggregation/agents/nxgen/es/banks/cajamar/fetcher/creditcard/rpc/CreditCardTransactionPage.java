package se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.creditcard.rpc;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class CreditCardTransactionPage implements TransactionKeyPaginatorResponse<String> {
    private final CajamarCreditCardTransactionsResponse creditCardTransactionsResponse;
    private final CreditCardAccount cardAccount;

    private CreditCardTransactionPage(
            CajamarCreditCardTransactionsResponse creditCardTransactionsResponse,
            CreditCardAccount cardAccount) {
        this.creditCardTransactionsResponse = creditCardTransactionsResponse;
        this.cardAccount = cardAccount;
    }

    public static CreditCardTransactionPage create(
            CajamarCreditCardTransactionsResponse transactionsResponse,
            CreditCardAccount creditCardAccount) {
        return new CreditCardTransactionPage(transactionsResponse, creditCardAccount);
    }

    @Override
    public String nextKey() {
        if (creditCardTransactionsResponse.isPaginationEqualNullOrEmpty()) {
            return null;
        }
        return creditCardTransactionsResponse.getNextPageKey();
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return creditCardTransactionsResponse
                .getRawTransactions()
                .map(transaction -> transaction.toTinkTransaction(cardAccount))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return creditCardTransactionsResponse.canFetchMore();
    }
}
