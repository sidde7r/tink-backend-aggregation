package se.tink.backend.core;

import java.util.Date;
import java.util.UUID;

public class InstrumentHistory {
    private UUID userId;
    private UUID portfolioId;
    private UUID instrumentId;
    private Date timestamp;
    private Double quantity;
    private Double averageAcquisitionPrice;
    private Double profit;
    private Double marketValue;

    private InstrumentHistory() {
    }

    public static InstrumentHistory createFromInstrumentAndTimestamp(Instrument instrument, Date timestamp) {
        InstrumentHistory history = new InstrumentHistory();
        history.setUserId(instrument.getUserId());
        history.setPortfolioId(instrument.getPortfolioId());
        history.setInstrumentId(instrument.getId());
        history.setTimestamp(timestamp);
        history.setQuantity(instrument.getQuantity());
        history.setAverageAcquisitionPrice(instrument.getAverageAcquisitionPrice());
        history.setProfit(instrument.getProfit());
        history.setMarketValue(instrument.getMarketValue());

        return history;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getPortfolioId() {
        return portfolioId;
    }

    public void setPortfolioId(UUID portfolioId) {
        this.portfolioId = portfolioId;
    }

    public UUID getInstrumentId() {
        return instrumentId;
    }

    public void setInstrumentId(UUID instrumentId) {
        this.instrumentId = instrumentId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public Double getAverageAcquisitionPrice() {
        return averageAcquisitionPrice;
    }

    public void setAverageAcquisitionPrice(Double averageAcquisitionPrice) {
        this.averageAcquisitionPrice = averageAcquisitionPrice;
    }

    public Double getProfit() {
        return profit;
    }

    public void setProfit(Double profit) {
        this.profit = profit;
    }

    public Double getMarketValue() {
        return marketValue;
    }

    public void setMarketValue(Double marketValue) {
        this.marketValue = marketValue;
    }
}
