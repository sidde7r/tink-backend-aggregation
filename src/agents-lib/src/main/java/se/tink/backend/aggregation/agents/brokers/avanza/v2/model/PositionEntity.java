package se.tink.backend.aggregation.agents.brokers.avanza.v2.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Optional;
import se.tink.backend.system.rpc.Instrument;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PositionEntity {
    private String orderbookId;
    private String name;
    private double value;
    private double lastPrice;
    private double changePercent;
    private String flagCode;
    private double profit;
    private double averageAcquiredPrice;
    private double collateralValue;
    private double volume;
    private double profitPercent;
    private double acquiredValue;
    private boolean tradable;
    private String currency;
    private double change;
    private String accountName;
    private String accountType;
    private String accountId;

    public String getOrderbookId() {
        return orderbookId;
    }

    public void setOrderbookId(String orderbookId) {
        this.orderbookId = orderbookId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public double getLastPrice() {
        return lastPrice;
    }

    public void setLastPrice(double lastPrice) {
        this.lastPrice = lastPrice;
    }

    public double getChangePercent() {
        return changePercent;
    }

    public void setChangePercent(double changePercent) {
        this.changePercent = changePercent;
    }

    public String getFlagCode() {
        return flagCode;
    }

    public void setFlagCode(String flagCode) {
        this.flagCode = flagCode;
    }

    public double getProfit() {
        return profit;
    }

    public void setProfit(double profit) {
        this.profit = profit;
    }

    public double getAverageAcquiredPrice() {
        return averageAcquiredPrice;
    }

    public void setAverageAcquiredPrice(double averageAcquiredPrice) {
        this.averageAcquiredPrice = averageAcquiredPrice;
    }

    public double getCollateralValue() {
        return collateralValue;
    }

    public void setCollateralValue(double collateralValue) {
        this.collateralValue = collateralValue;
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public double getProfitPercent() {
        return profitPercent;
    }

    public void setProfitPercent(double profitPercent) {
        this.profitPercent = profitPercent;
    }

    public double getAcquiredValue() {
        return acquiredValue;
    }

    public void setAcquiredValue(double acquiredValue) {
        this.acquiredValue = acquiredValue;
    }

    public boolean isTradable() {
        return tradable;
    }

    public void setTradable(boolean tradable) {
        this.tradable = tradable;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public double getChange() {
        return change;
    }

    public void setChange(double change) {
        this.change = change;
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

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public Optional<Instrument> toInstrument(String instrumentType, String market, String isin) {
        if (getValue() == 0) {
            return Optional.empty();
        }

        Instrument instrument = new Instrument();

        instrument.setAverageAcquisitionPrice(getAverageAcquiredPrice());
        instrument.setCurrency(getCurrency());
        instrument.setIsin(isin);
        instrument.setMarketPlace(market);
        instrument.setMarketValue(getValue());
        instrument.setName(getName());
        instrument.setPrice(getLastPrice());
        instrument.setProfit(getProfit());
        instrument.setQuantity(getVolume());
        instrument.setRawType(instrumentType);
        instrument.setType(getInstrumentType(instrumentType));
        // Since we don't get the isin from this entity we have to enrich the instrument in a later
        // stage.
        // This is done by matching the order book id of the transactions of the specific
        // instrument.
        instrument.setUniqueIdentifier(getOrderbookId());

        return Optional.of(instrument);
    }

    private Instrument.Type getInstrumentType(String instrumentType) {
        switch (instrumentType.toLowerCase()) {
            case "stock":
                return Instrument.Type.STOCK;
            case "fund":
                return Instrument.Type.FUND;
            case "bond":
            case "option":
            case "future_forward":
            case "certificate":
            case "warrant":
            case "exchange_traded_fund":
            case "index":
            case "premium_bond":
            case "subscription_option":
            case "equity_linked_bond":
            case "convertible":
                // Intentional fall through
            default:
                return Instrument.Type.OTHER;
        }
    }
}
