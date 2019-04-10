package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.accounts.creditcard;

import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class JyskeCreditCardTransactionFetcher
        implements TransactionPagePaginator<CreditCardAccount> {

    @Override
    public PaginatorResponse getTransactionsFor(CreditCardAccount account, int page) {
        return PaginatorResponseImpl.createEmpty(false);
    }
}
