package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v1.fetcher.investment.rpc;

import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v1.SpankkiConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v1.fetcher.investment.entities.PortfolioEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v1.rpc.SpankkiResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class FundsPortfoliosResponse extends SpankkiResponse {
    private List<PortfolioEntity> portfolios;
    private boolean showCreateNewPortfolioOption;

    public List<PortfolioEntity> getPortfolios() {
        return portfolios;
    }

    public boolean isShowCreateNewPortfolioOption() {
        return showCreateNewPortfolioOption;
    }

    public Collection<InvestmentAccount> toTinkInvestmentAccounts(
            Map<String, String> fundIdIsinMapper) {
        List<InvestmentAccount> investmentAccounts = new ArrayList<>();

        if (portfolios == null) {
            return investmentAccounts;
        }

        Map<String, AccountData> accounts = new HashMap<>();

        // group portfolios by customer id, kind of account
        for (PortfolioEntity portfolio : portfolios) {
            if (portfolio.isPortfolio()) {
                String accountId =
                        SpankkiConstants.Investment.ACCOUNT_ID_PREFIX + portfolio.getCustomerId();

                AccountData accountData = accounts.get(accountId);
                // new account
                if (accountData == null) {
                    accountData = new AccountData(accountId, portfolio.getPortfolioName());
                    accounts.put(accountId, accountData);
                }

                // sum balance
                accountData.balance += portfolio.getTotalValue();
                if (!Strings.isNullOrEmpty(portfolio.getCurrencyCode())) {
                    accountData.currency = portfolio.getCurrencyCode();
                }
                // add portfolio to account
                accountData.portfolios.add(portfolio.toTinkPortfolio(fundIdIsinMapper));
            }
        }

        // aggregate portfolio data to account level
        for (Map.Entry<String, AccountData> accountEntry : accounts.entrySet()) {
            AccountData accountData = accountEntry.getValue();

            investmentAccounts.add(
                    InvestmentAccount.builder(accountData.id)
                            .setCashBalance(ExactCurrencyAmount.zero(accountData.currency))
                            .setAccountNumber(accountData.id)
                            .setPortfolios(accountData.portfolios)
                            .setName(accountData.name)
                            .setBankIdentifier(accountData.id)
                            .build());
        }
        return investmentAccounts;
    }

    private static class AccountData {
        String id;
        List<Portfolio> portfolios = new ArrayList<>();
        String name;
        String currency = "EUR";
        double balance = 0.0;

        AccountData(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}
