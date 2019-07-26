package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher;

import java.util.Set;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Configuration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.utils.AmericanExpressV62Storage;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class AmericanExpressV62TransactionFetcher
        implements TransactionPagePaginator<CreditCardAccount> {

    private final AmericanExpressV62Configuration config;
    private final AmericanExpressV62Storage instanceStorage;

    private AmericanExpressV62TransactionFetcher(
            AmericanExpressV62Configuration config,
            final AmericanExpressV62Storage instanceStorage) {
        this.config = config;
        this.instanceStorage = instanceStorage;
    }

    public static AmericanExpressV62TransactionFetcher create(
            AmericanExpressV62Configuration config, AmericanExpressV62Storage instanceStorage) {
        return new AmericanExpressV62TransactionFetcher(config, instanceStorage);
    }

    @Override
    /** Fetches the transactions from saved data. */
    public PaginatorResponse getTransactionsFor(CreditCardAccount account, int page) {
        Set<TransactionEntity> transactionEntitySet =
                instanceStorage.getAccountTransactions(account.getAccountNumber());

        Set<Transaction> transactions =
                transactionEntitySet.stream()
                        .map(transactionEntity -> transactionEntity.toTransaction(config))
                        .collect(Collectors.toSet());

        return PaginatorResponseImpl.create(transactions, false);
    }
}
