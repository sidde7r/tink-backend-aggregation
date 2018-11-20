package se.tink.backend.aggregation.nxgen.agents.demo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.agents.utils.demo.DemoDataUtils;
import se.tink.backend.aggregation.nxgen.agents.demo.demogenerator.InvestmentGenerator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginator;
import se.tink.backend.aggregation.nxgen.core.account.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.core.Amount;
import se.tink.backend.system.rpc.Portfolio;


public class NextGenerationDemoInvestmentFetcher implements AccountFetcher<InvestmentAccount>,
        TransactionPaginator<InvestmentAccount> {
    //Move to constants
    private static final String INVESTMENT_ACCOUNT_ID = "9999-444444444444";
    //Move to constants
    private double investmentAccountBalance = 123456;
    private String currency;

    public NextGenerationDemoInvestmentFetcher(String currency) {
        this.currency = currency;
        this.investmentAccountBalance = NextGenDemoConstants.getSekToCurrencyConverter(currency) * investmentAccountBalance;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        List<InvestmentAccount> investmentAccounts = new ArrayList<>();
        investmentAccounts.add(
            InvestmentAccount.builder(INVESTMENT_ACCOUNT_ID)
                    .setBalance(new Amount(currency, investmentAccountBalance))
                    .setName("")
                    .setAccountNumber(INVESTMENT_ACCOUNT_ID)
                    .setPortfolios(InvestmentGenerator.generateFakePortfolios(INVESTMENT_ACCOUNT_ID, investmentAccountBalance))
                    .setCashBalance(new Amount(currency, investmentAccountBalance))
                    .build()
        );

        return investmentAccounts;
    }

    //TODO Implement fake transactions
    @Override
    public PaginatorResponse fetchTransactionsFor(InvestmentAccount account) {
        return PaginatorResponseImpl.createEmpty(false);
    }
}
