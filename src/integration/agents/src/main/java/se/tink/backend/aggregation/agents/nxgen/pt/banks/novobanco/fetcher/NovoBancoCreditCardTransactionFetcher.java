package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher;

import java.util.Collections;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class NovoBancoCreditCardTransactionFetcher
        implements TransactionPagePaginator<CreditCardAccount> {
    @Override
    public PaginatorResponse getTransactionsFor(CreditCardAccount account, int page) {
        // fetching credit card transactions is not yet supported
        return PaginatorResponseImpl.create(Collections.emptyList(), false);
    }
}
