package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.rpc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.entities.PositionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EquityLinkedBondMarketInfoResponse {
    private double loanFactor;
    private String marketPlace;
    private int numberOfPriceAlerts;
    private boolean pushPermitted;
    private String currency;
    private String name;
    private String id;
    private boolean tradable;
    private double lastPrice;
    private String lastPriceUpdated;
    private double change;
    private double changePercent;
    private long totalVolumeTraded;
    private double totalValueTraded;
    private String tickerSymbol;
    private String flagCode;
    private String preliminaryFeesType;
    private String quoteUpdated;
    private double priceOneWeekAgo;
    private double priceOneMonthAgo;
    private double priceSixMonthsAgo;
    private double priceAtStartOfYear;
    private double priceOneYearAgo;
    private double priceThreeYearsAgo;
    private double priceThreeMonthsAgo;
    private List<PositionEntity> positions;
    private double positionsTotalValue;
    private String isin;
    private long tradingUnit;

    public double getLoanFactor() {
        return loanFactor;
    }

    public String getMarketPlace() {
        return marketPlace;
    }

    public int getNumberOfPriceAlerts() {
        return numberOfPriceAlerts;
    }

    public boolean isPushPermitted() {
        return pushPermitted;
    }

    public String getCurrency() {
        return currency;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public boolean isTradable() {
        return tradable;
    }

    public double getLastPrice() {
        return lastPrice;
    }

    public String getLastPriceUpdated() {
        return lastPriceUpdated;
    }

    public double getChange() {
        return change;
    }

    public double getChangePercent() {
        return changePercent;
    }

    public long getTotalVolumeTraded() {
        return totalVolumeTraded;
    }

    public double getTotalValueTraded() {
        return totalValueTraded;
    }

    public String getTickerSymbol() {
        return tickerSymbol;
    }

    public String getFlagCode() {
        return flagCode;
    }

    public String getPreliminaryFeesType() {
        return preliminaryFeesType;
    }

    public String getQuoteUpdated() {
        return quoteUpdated;
    }

    public double getPriceOneWeekAgo() {
        return priceOneWeekAgo;
    }

    public double getPriceOneMonthAgo() {
        return priceOneMonthAgo;
    }

    public double getPriceSixMonthsAgo() {
        return priceSixMonthsAgo;
    }

    public double getPriceAtStartOfYear() {
        return priceAtStartOfYear;
    }

    public double getPriceOneYearAgo() {
        return priceOneYearAgo;
    }

    public double getPriceThreeYearsAgo() {
        return priceThreeYearsAgo;
    }

    public double getPriceThreeMonthsAgo() {
        return priceThreeMonthsAgo;
    }

    public List<PositionEntity> getPositions() {
        return Optional.ofNullable(positions).orElse(Collections.emptyList());
    }

    public double getPositionsTotalValue() {
        return positionsTotalValue;
    }

    public String getIsin() {
        return isin;
    }

    public long getTradingUnit() {
        return tradingUnit;
    }
}
