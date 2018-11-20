package se.tink.backend.aggregation.nxgen.agents.demo.fetchers;

import java.util.Collection;
import se.tink.backend.aggregation.nxgen.agents.demo.demogenerator.LoanGenerator;
import se.tink.backend.aggregation.nxgen.agents.demo.definitions.DemoLoanAccountDefinition;
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
    private final DemoLoanAccountDefinition accountDefinition;

    public NextGenerationDemoLoanFetcher(String currency, Catalog catalog, DemoLoanAccountDefinition accountDefinition) {
        this.currency = currency;
        this.catalog = catalog;
        this.accountDefinition = accountDefinition;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        return LoanGenerator.fetchLoanAccounts(currency, catalog, accountDefinition);
    }

    //TODO Implement fake transactions
    @Override
    public PaginatorResponse fetchTransactionsFor(LoanAccount account) {
        return PaginatorResponseImpl.createEmpty(false);
    }
}
