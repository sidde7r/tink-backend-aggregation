package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.rpc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.entities.PositionEntity;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.entities.UnderlyingOrderbookEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class WarrantMarketInfoResponse {
    private double leverage;
    private String marketPlace;
    private int numberOfPriceAlerts;
    private boolean pushPermitted;
    private String currency;
    private String name;
    private String id;
    private boolean tradable;
    private double buyPrice;
    private double sellPrice;
    private double highestPrice;
    private double lowestPrice;
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
    private double priceThreeMonthsAgo;
    private UnderlyingOrderbookEntity underlyingOrderbook;
    private List<PositionEntity> positions;
    private double positionsTotalValue;
    private double spread;
    private String underlyingCurrency;
    private String direction;
    private double parity;
    private double barrierPrice;
    private double financingLevel;
    private String issuerName;
    private String finalTerms;
    private String callIndicator;
    private String warrantType;

    public double getLeverage() {
        return leverage;
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

    public double getBuyPrice() {
        return buyPrice;
    }

    public double getSellPrice() {
        return sellPrice;
    }

    public double getHighestPrice() {
        return highestPrice;
    }

    public double getLowestPrice() {
        return lowestPrice;
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

    public double getPriceThreeMonthsAgo() {
        return priceThreeMonthsAgo;
    }

    public UnderlyingOrderbookEntity getUnderlyingOrderbook() {
        return underlyingOrderbook;
    }

    public List<PositionEntity> getPositions() {
        return Optional.ofNullable(positions).orElse(Collections.emptyList());
    }

    public double getPositionsTotalValue() {
        return positionsTotalValue;
    }

    public double getSpread() {
        return spread;
    }

    public String getUnderlyingCurrency() {
        return underlyingCurrency;
    }

    public String getDirection() {
        return direction;
    }

    public double getParity() {
        return parity;
    }

    public double getBarrierPrice() {
        return barrierPrice;
    }

    public double getFinancingLevel() {
        return financingLevel;
    }

    public String getIssuerName() {
        return issuerName;
    }

    public String getFinalTerms() {
        return finalTerms;
    }

    public String getCallIndicator() {
        return callIndicator;
    }

    public String getWarrantType() {
        return warrantType;
    }
}
