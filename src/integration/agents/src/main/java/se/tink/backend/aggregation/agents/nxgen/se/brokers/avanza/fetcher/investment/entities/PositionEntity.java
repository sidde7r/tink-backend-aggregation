package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaConstants.InstrumentTypes;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.id.InstrumentIdModule;

@JsonObject
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

    @JsonIgnore
    public InstrumentModule toTinkInstrument(
            InstrumentEntity parent, String marketPlace, String isin) {

        // Since we don't get the isin from this entity we have to enrich the instrument in a later
        // stage.
        // This is done by matching the order book id of the transactions of the specific
        // instrument.
        return InstrumentModule.builder()
                .withType(getTinkInstrumentType(parent.getInstrumentType()))
                .withId(InstrumentIdModule.of(isin, marketPlace, name))
                .withMarketPrice(lastPrice)
                .withMarketValue(value)
                .withAverageAcquisitionPrice(averageAcquiredPrice)
                .withCurrency(currency)
                .withQuantity(volume)
                .withProfit(profit)
                .setRawType(parent.getInstrumentType())
                .build();
    }

    @JsonIgnore
    private InstrumentModule.InstrumentType getTinkInstrumentType(String instrumentType) {
        switch (instrumentType.toLowerCase()) {
            case InstrumentTypes.STOCK:
                return InstrumentModule.InstrumentType.STOCK;
            case InstrumentTypes.AUTO_PORTFOLIO:
            case InstrumentTypes.FUND:
                return InstrumentModule.InstrumentType.FUND;
            case InstrumentTypes.BOND:
            case InstrumentTypes.OPTION:
            case InstrumentTypes.FUTURE_FORWARD:
            case InstrumentTypes.CERTIFICATE:
            case InstrumentTypes.WARRANT:
            case InstrumentTypes.EXCHANGE_TRADED_FUND:
            case InstrumentTypes.INDEX:
            case InstrumentTypes.PREMIUM_BOND:
            case InstrumentTypes.SUBSCRIPTION_OPTION:
            case InstrumentTypes.EQUITY_LINKED_BOND:
            case InstrumentTypes.CONVERTIBLE:
                // Intentional fall through
            default:
                return InstrumentModule.InstrumentType.OTHER;
        }
    }
}
