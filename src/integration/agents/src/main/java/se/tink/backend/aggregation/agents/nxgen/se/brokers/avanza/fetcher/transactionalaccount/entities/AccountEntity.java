package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.transactionalaccount.entities;

import static se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaConstants.MAPPERS;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountEntity {
    private double interestRate;
    private double totalProfit;
    private double totalBalance;
    private String accountType;
    private boolean active;
    private double totalBalanceDue;
    private double ownCapital;
    private String accountId;
    private double performancePercent;
    private double performance;
    private boolean depositable;
    private double buyingPower;
    private boolean attorney;
    private boolean tradable;
    private String name;
    private boolean accountPartlyOwned;
    private double totalProfitPercent;
    private String sparkontoPlusType;

    public double getInterestRate() {
        return interestRate;
    }

    public double getTotalProfit() {
        return totalProfit;
    }

    public double getTotalBalance() {
        return totalBalance;
    }

    public String getAccountType() {
        return accountType;
    }

    public boolean isActive() {
        return active;
    }

    public double getTotalBalanceDue() {
        return totalBalanceDue;
    }

    public double getOwnCapital() {
        return ownCapital;
    }

    public String getAccountId() {
        return accountId;
    }

    public double getPerformancePercent() {
        return performancePercent;
    }

    public double getPerformance() {
        return performance;
    }

    public boolean isDepositable() {
        return depositable;
    }

    public double getBuyingPower() {
        return buyingPower;
    }

    public boolean isAttorney() {
        return attorney;
    }

    public boolean isTradable() {
        return tradable;
    }

    public String getName() {
        return name;
    }

    public boolean isAccountPartlyOwned() {
        return accountPartlyOwned;
    }

    public double getTotalProfitPercent() {
        return totalProfitPercent;
    }

    public String getSparkontoPlusType() {
        return sparkontoPlusType;
    }

    @JsonIgnore
    public boolean isTransactionalAccount() {
        return MAPPERS.isTransactionalAccount(getAccountType());
    }

    @JsonIgnore
    public boolean isLoanAccount() {
        return MAPPERS.isLoanAccount(getAccountType());
    }

    @JsonIgnore
    public boolean isInvestmentAccount() {
        return MAPPERS.isInvestmentAccount(getAccountType());
    }

    @JsonIgnore
    public boolean isPensionAccount() {
        return MAPPERS.isPensionAccount(getAccountType());
    }
}
