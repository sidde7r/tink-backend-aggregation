package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.accounts.creditcard;

import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;

public class JyskeCreditCardTransactionFetcher implements TransactionPagePaginator<CreditCardAccount> {

    @Override
    public TransactionPagePaginatorResponse getTransactionsFor(CreditCardAccount account, int page) {
        return null;
    }
}
