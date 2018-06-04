package se.tink.backend.core;

import java.util.Date;
import java.util.UUID;

public class PortfolioHistory {
    private UUID userId;
    private UUID accountId;
    private Date timestamp;
    private UUID portfolioId;
    private Double totalValue;
    private Double totalProfit;
    private Double cashValue;

    public static PortfolioHistory createFromPortfolioAndTimestamp(Portfolio portfolio, Date timestamp) {
        PortfolioHistory portfolioHistory = new PortfolioHistory();

        portfolioHistory.setUserId(portfolio.getUserId());
        portfolioHistory.setAccountId(portfolio.getAccountId());
        portfolioHistory.setPortfolioId(portfolio.getId());
        portfolioHistory.setTotalProfit(portfolio.getTotalProfit());
        portfolioHistory.setTotalValue(portfolio.getTotalValue());
        portfolioHistory.setCashValue(portfolio.getCashValue());
        portfolioHistory.setTimestamp(timestamp);

        return portfolioHistory;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public UUID getPortfolioId() {
        return portfolioId;
    }

    public void setPortfolioId(UUID portfolioId) {
        this.portfolioId = portfolioId;
    }

    public Double getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(Double totalValue) {
        this.totalValue = totalValue;
    }

    public Double getTotalProfit() {
        return totalProfit;
    }

    public void setTotalProfit(Double totalProfit) {
        this.totalProfit = totalProfit;
    }

    public Double getCashValue() {
        return cashValue;
    }

    public void setCashValue(Double cashValue) {
        this.cashValue = cashValue;
    }
}
