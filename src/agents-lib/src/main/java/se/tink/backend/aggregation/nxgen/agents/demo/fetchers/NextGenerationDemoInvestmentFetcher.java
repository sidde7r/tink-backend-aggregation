package se.tink.backend.aggregation.nxgen.agents.demo.fetchers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import se.tink.backend.aggregation.nxgen.agents.demo.DemoConstants;
import se.tink.backend.aggregation.nxgen.agents.demo.demogenerator.InvestmentGenerator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginator;
import se.tink.backend.aggregation.nxgen.core.account.InvestmentAccount;
import se.tink.backend.core.Amount;

public class NextGenerationDemoInvestmentFetcher implements AccountFetcher<InvestmentAccount>,
        TransactionPaginator<InvestmentAccount> {

    private String currency;

    public NextGenerationDemoInvestmentFetcher(String currency) {
        this.currency = currency;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        return InvestmentGenerator.fetchInvestmentAccounts(currency);
    }

    //TODO Implement fake transactions
    @Override
    public PaginatorResponse fetchTransactionsFor(InvestmentAccount account) {
        return PaginatorResponseImpl.createEmpty(false);
    }
}
