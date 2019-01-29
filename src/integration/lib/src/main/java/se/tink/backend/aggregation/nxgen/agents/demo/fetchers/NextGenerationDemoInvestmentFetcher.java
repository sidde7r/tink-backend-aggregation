package se.tink.backend.aggregation.nxgen.agents.demo.fetchers;

import java.util.Collection;
import se.tink.backend.aggregation.nxgen.agents.demo.demogenerator.InvestmentGenerator;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoInvestmentAccount;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginator;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;

public class NextGenerationDemoInvestmentFetcher implements AccountFetcher<InvestmentAccount>,
        TransactionPaginator<InvestmentAccount> {

    private String currency;
    private DemoInvestmentAccount accountDefinition;

    public NextGenerationDemoInvestmentFetcher(String currency, DemoInvestmentAccount accountDefinition) {
        this.currency = currency;
        this.accountDefinition = accountDefinition;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        return InvestmentGenerator.fetchInvestmentAccounts(currency, accountDefinition);
    }

    //TODO Implement fake transactions
    @Override
    public void resetState() {

    }

    @Override
    public PaginatorResponse fetchTransactionsFor(InvestmentAccount account) {
        return PaginatorResponseImpl.createEmpty(false);
    }
}
