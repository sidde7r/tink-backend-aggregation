package se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.fetchers.investmentfetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collections;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class FundAccountEntity {
    private String accountNumber;
    private String accountName;
    private double balance;
    private boolean cancelled;
    private boolean defaultAccount;
    private boolean cancellable;
    private String bankAccountNumber;
    private boolean accountSuspended;
    private int investment;
    private int maxInvestment;
    private int notSettledInvestment;
    private boolean ask;
    private boolean ips;

    @JsonIgnore private double accountBalance;

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getAccountName() {
        return accountName;
    }

    public double getBalance() {
        return balance;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public boolean isDefaultAccount() {
        return defaultAccount;
    }

    public boolean isCancellable() {
        return cancellable;
    }

    public String getBankAccountNumber() {
        return bankAccountNumber;
    }

    public boolean isAccountSuspended() {
        return accountSuspended;
    }

    public int getInvestment() {
        return investment;
    }

    public int getMaxInvestment() {
        return maxInvestment;
    }

    public int getNotSettledInvestment() {
        return notSettledInvestment;
    }

    public boolean isAsk() {
        return ask;
    }

    public boolean isIps() {
        return ips;
    }

    public void setAccountBalance(double accountBalance) {
        this.accountBalance = accountBalance;
    }

    public double getAccountBalance() {
        return accountBalance;
    }

    private Portfolio toTinkPortfolio() {
        Portfolio portfolio = new Portfolio();

        portfolio.setType(Portfolio.Type.DEPOT);
        portfolio.setTotalValue(balance);
        portfolio.setRawType(accountName);
        portfolio.setUniqueIdentifier(accountNumber);

        return portfolio;
    }

    public InvestmentAccount toInvestmentAccount() {
        return InvestmentAccount.builder(accountNumber)
                .setAccountNumber(accountNumber)
                .setName(accountName)
                .setCashBalance(ExactCurrencyAmount.zero("NOK"))
                .setPortfolios(Collections.singletonList(toTinkPortfolio()))
                .build();
    }
}
