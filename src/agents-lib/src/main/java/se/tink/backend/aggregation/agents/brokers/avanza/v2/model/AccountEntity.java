package se.tink.backend.aggregation.agents.brokers.avanza.v2.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountEntity {
    private String accountId;
    private boolean accountPartlyOwned;
    private String accountType;
    private boolean attorney;
    private double buyingPower;
    private double interestRate;
    private String name;
    private double ownCapital;
    private double performance;
    private double performancePercent;
    private double totalBalance;
    private double totalBalanceDue;
    private String sparkontoPlusType;
    private boolean tradable;
    private boolean depositable;
    private boolean active;
    private double totalProfitPercent;
    private double totalProfit;

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public double getBuyingPower() {
        return buyingPower;
    }

    public void setBuyingPower(double buyingPower) {
        this.buyingPower = buyingPower;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getOwnCapital() {
        return ownCapital;
    }

    public void setOwnCapital(double ownCapital) {
        this.ownCapital = ownCapital;
    }

    public double getPerformance() {
        return performance;
    }

    public void setPerformance(double performance) {
        this.performance = performance;
    }

    public double getPerformancePercent() {
        return performancePercent;
    }

    public void setPerformancePercent(double performancePercent) {
        this.performancePercent = performancePercent;
    }

    public double getTotalBalance() {
        return totalBalance;
    }

    public void setTotalBalance(double totalBalance) {
        this.totalBalance = totalBalance;
    }

    public double getTotalBalanceDue() {
        return totalBalanceDue;
    }

    public void setTotalBalanceDue(double totalBalanceDue) {
        this.totalBalanceDue = totalBalanceDue;
    }

    public boolean isAccountPartlyOwned() {
        return accountPartlyOwned;
    }

    public void setAccountPartlyOwned(boolean accountPartlyOwned) {
        this.accountPartlyOwned = accountPartlyOwned;
    }

    public boolean isAttorney() {
        return attorney;
    }

    public void setAttorney(boolean attorney) {
        this.attorney = attorney;
    }

    public boolean isTradable() {
        return tradable;
    }

    public void setTradable(boolean tradable) {
        this.tradable = tradable;
    }

    public String getSparkontoPlusType() {
        return sparkontoPlusType;
    }

    public void setSparkontoPlusType(String sparkontoPlusType) {
        this.sparkontoPlusType = sparkontoPlusType;
    }

    public boolean isDepositable() {
        return depositable;
    }

    public void setDepositable(boolean depositable) {
        this.depositable = depositable;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public double getTotalProfitPercent() {
        return totalProfitPercent;
    }

    public void setTotalProfitPercent(double totalProfitPercent) {
        this.totalProfitPercent = totalProfitPercent;
    }

    public double getTotalProfit() {
        return totalProfit;
    }

    public void setTotalProfit(double totalProfit) {
        this.totalProfit = totalProfit;
    }
}
