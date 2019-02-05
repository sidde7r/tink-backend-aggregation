package se.tink.backend.aggregation.agents.brokers.avanza.v2.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import se.tink.backend.aggregation.agents.brokers.avanza.v2.model.PositionAggregationEntity;
import se.tink.backend.aggregation.agents.models.Portfolio;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PositionResponse {
    private List<PositionAggregationEntity> instrumentPositions;
    private double totalProfit;
    private double totalOwnCapital;
    private double totalBuyingPower;
    private double totalProfitPercent;
    private double totalBalance;
    private String accountName;
    private String accountType;
    private boolean depositable;
    private String accountId;

    public List<PositionAggregationEntity> getInstrumentPositions() {
        return instrumentPositions;
    }

    public void setInstrumentPositions(List<PositionAggregationEntity> instrumentPositions) {
        this.instrumentPositions = instrumentPositions;
    }

    public double getTotalProfit() {
        return totalProfit;
    }

    public void setTotalProfit(double totalProfit) {
        this.totalProfit = totalProfit;
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

    public double getTotalProfitPercent() {
        return totalProfitPercent;
    }

    public void setTotalProfitPercent(double totalProfitPercent) {
        this.totalProfitPercent = totalProfitPercent;
    }

    public double getTotalBalance() {
        return totalBalance;
    }

    public void setTotalBalance(double totalBalance) {
        this.totalBalance = totalBalance;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public boolean isDepositable() {
        return depositable;
    }

    public void setDepositable(boolean depositable) {
        this.depositable = depositable;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public Portfolio toPortfolio() {
        Portfolio portfolio = new Portfolio();

        portfolio.setRawType(getAccountType());
        portfolio.setTotalProfit(getTotalProfit());
        portfolio.setTotalValue(getTotalOwnCapital());
        portfolio.setType(getPortfolioType());
        portfolio.setUniqueIdentifier(getAccountId());

        return portfolio;
    }

    private Portfolio.Type getPortfolioType() {
        switch (getAccountType().toLowerCase()) {
            case "investeringssparkonto":
                return Portfolio.Type.ISK;
            case "aktiefondkonto":
                return Portfolio.Type.DEPOT;
            case "tjanstepension":
                return Portfolio.Type.PENSION;
            case "kapitalforsakring":
                return Portfolio.Type.KF;
            default:
                return Portfolio.Type.OTHER;
        }
    }
}
