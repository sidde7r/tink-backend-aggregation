package se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.fetcher.creditcard;

import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

/**
 * Extension of the date pagination controller, because for Norwegian we need to fetch uninvoiced
 * transactions separately before starting date pagination.
 */
public class NorwegianDatePaginationController
        extends TransactionDatePaginationController<CreditCardAccount> {

    private final NorwegianCreditCardFetcher fetcher;
    private boolean hasFetchedUninvoiced = false;

    public NorwegianDatePaginationController(
            TransactionDatePaginator<CreditCardAccount> paginator,
            NorwegianCreditCardFetcher fetcher) {
        super(new TransactionDatePaginationController.Builder<>(paginator));
        this.fetcher = fetcher;
    }

    @Override
    public void resetState() {
        super.resetState();
        hasFetchedUninvoiced = false;
    }

    @Override
    public PaginatorResponse fetchTransactionsFor(CreditCardAccount account) {
        if (!hasFetchedUninvoiced) {
            hasFetchedUninvoiced = true;
            return fetcher.fetchUninvoicedTransactions();
        }

        return super.fetchTransactionsFor(account);
    }
}
