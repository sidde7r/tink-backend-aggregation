package se.tink.backend.aggregation.nxgen.agents.demo;

import java.util.Collection;
import se.tink.backend.aggregation.nxgen.agents.demo.demogenerator.LoanGenerator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginator;
import se.tink.backend.aggregation.nxgen.core.account.LoanAccount;
import se.tink.libraries.i18n.Catalog;

//TODO Add transaction fetching
public class NextGenerationDemoLoanFetcher  implements AccountFetcher<LoanAccount>, TransactionPaginator<LoanAccount> {
    private final String currency;
    private final Catalog catalog;

    public NextGenerationDemoLoanFetcher(String currency, Catalog catalog) {
        this.currency = currency;
        this.catalog = catalog;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        return LoanGenerator.fetchLoanAccounts(currency, catalog);
    }

    //TODO Implement fake transactions
    @Override
    public PaginatorResponse fetchTransactionsFor(LoanAccount account) {
        return PaginatorResponseImpl.createEmpty(false);
    }
}
