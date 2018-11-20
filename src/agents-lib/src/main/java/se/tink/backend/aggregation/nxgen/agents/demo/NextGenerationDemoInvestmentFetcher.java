package se.tink.backend.aggregation.nxgen.agents.demo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
    private double investmentAccountBalance;

    public NextGenerationDemoInvestmentFetcher(String currency) {
        this.currency = currency;
        this.investmentAccountBalance = DemoConstants.getSekToCurrencyConverter(
                        currency,DemoConstants.InvestmentAccountInformation.INVESTMENT_BALANCE);
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        List<InvestmentAccount> investmentAccounts = new ArrayList<>();
        investmentAccounts.add(
            InvestmentAccount.builder(DemoConstants.InvestmentAccountInformation.INVESTMENT_ACCOUNT_ID)
                    .setBalance(new Amount(currency, investmentAccountBalance))
                    .setName("")
                    .setAccountNumber(DemoConstants.InvestmentAccountInformation.INVESTMENT_ACCOUNT_ID)
                    .setPortfolios(InvestmentGenerator.generateFakePortfolios(
                            DemoConstants.InvestmentAccountInformation.INVESTMENT_ACCOUNT_ID, investmentAccountBalance))
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
