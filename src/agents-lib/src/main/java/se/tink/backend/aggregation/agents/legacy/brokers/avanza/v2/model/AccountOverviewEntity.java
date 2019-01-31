package se.tink.backend.aggregation.agents.brokers.avanza.v2.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountOverviewEntity {
    private List<AccountEntity> accounts;
    private double numberOfOrders;
    private double numberOfDeals;
    private double totalPerformance;
    private double totalPerformancePercent;
    private double totalOwnCapital;
    private double totalBuyingPower;
    private double totalBalance;

    public double getNumberOfOrders() {
        return numberOfOrders;
    }

    public void setNumberOfOrders(double numberOfOrders) {
        this.numberOfOrders = numberOfOrders;
    }

    public double getNumberOfDeals() {
        return numberOfDeals;
    }

    public void setNumberOfDeals(double numberOfDeals) {
        this.numberOfDeals = numberOfDeals;
    }

    public double getTotalPerformance() {
        return totalPerformance;
    }

    public void setTotalPerformance(double totalPerformance) {
        this.totalPerformance = totalPerformance;
    }

    public double getTotalPerformancePercent() {
        return totalPerformancePercent;
    }

    public void setTotalPerformancePercent(double totalPerformancePercent) {
        this.totalPerformancePercent = totalPerformancePercent;
    }

    public double getTotalOwnCapital() {
        return totalOwnCapital;
    }

    public void setTotalOwnCapital(double totalOwnCapital) {
        this.totalOwnCapital = totalOwnCapital;
    }

    public double getTotalBuyingPower() {
        return totalBuyingPower;
    }

    public void setTotalBuyingPower(double totalBuyingPower) {
        this.totalBuyingPower = totalBuyingPower;
    }

    public double getTotalBalance() {
        return totalBalance;
    }

    public void setTotalBalance(double totalBalance) {
        this.totalBalance = totalBalance;
    }

    public List<AccountEntity> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<AccountEntity> accounts) {
        this.accounts = accounts;
    }
}
