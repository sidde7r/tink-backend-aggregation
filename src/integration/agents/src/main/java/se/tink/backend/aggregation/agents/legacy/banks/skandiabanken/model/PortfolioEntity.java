package se.tink.backend.aggregation.agents.banks.skandiabanken.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PortfolioEntity {
    private String id;
    private int portfolioType;
    private int buyStatus;
    private int sellStatus;
    private int exchangeStatus;
    private long disposableAmountInCents;
    private String portfolioStyle;
    private List<HoldingEntity> holdings;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getPortfolioType() {
        return portfolioType;
    }

    public void setPortfolioType(int portfolioType) {
        this.portfolioType = portfolioType;
    }

    public int getBuyStatus() {
        return buyStatus;
    }

    public void setBuyStatus(int buyStatus) {
        this.buyStatus = buyStatus;
    }

    public int getSellStatus() {
        return sellStatus;
    }

    public void setSellStatus(int sellStatus) {
        this.sellStatus = sellStatus;
    }

    public int getExchangeStatus() {
        return exchangeStatus;
    }

    public void setExchangeStatus(int exchangeStatus) {
        this.exchangeStatus = exchangeStatus;
    }

    public long getDisposableAmountInCents() {
        return disposableAmountInCents;
    }

    public void setDisposableAmountInCents(long disposableAmountInCents) {
        this.disposableAmountInCents = disposableAmountInCents;
    }

    public String getPortfolioStyle() {
        return portfolioStyle;
    }

    public void setPortfolioStyle(String portfolioStyle) {
        this.portfolioStyle = portfolioStyle;
    }

    public List<HoldingEntity> getHoldings() {
        if (holdings == null) {
            return Collections.emptyList();
        }

        return holdings;
    }

    public void setHoldings(List<HoldingEntity> holdings) {
        this.holdings = holdings;
    }
}
