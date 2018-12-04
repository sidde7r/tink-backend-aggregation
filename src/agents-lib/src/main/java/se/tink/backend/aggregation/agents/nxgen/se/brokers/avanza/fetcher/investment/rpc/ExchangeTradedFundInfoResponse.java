package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.rpc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.brokers.avanza.v2.model.PositionEntity;
import se.tink.backend.aggregation.agents.brokers.avanza.v2.model.UnderlyingOrderbookEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ExchangeTradedFundInfoResponse {
    private double loanFactor;
    private String marketPlace;
    private int numberOfPriceAlerts;
    private boolean pushPermitted;
    private String currency;
    private String flagCode;
    private boolean tradable;
    private String quoteUpdated;
    private double highestPrice;
    private double lowestPrice;
    private double lastPrice;
    private String lastPriceUpdated;
    private double change;
    private double changePercent;
    private long totalVolumeTraded;
    private double totalValueTraded;
    private String preliminaryFeesType;
    private String tickerSymbol;
    private String name;
    private String id;
    private double priceOneWeekAgo;
    private double priceOneMonthAgo;
    private double priceSixMonthsAgo;
    private double priceAtStartOfYear;
    private double priceOneYearAgo;
    private double priceThreeYearsAgo;
    private double priceFiveYearsAgo;
    private double priceThreeMonthsAgo;
    private UnderlyingOrderbookEntity underlyingOrderbook;
    private List<PositionEntity> positions;
    private double positionsTotalValue;
    private String assetRootCategory;
    private String assetSubCategory;
    private String assetSubSubCategory;
    private String issuerName;
    private String prospectus;
    private double managementFee;
    private double leverage;
    private String startDate;
    private String direction;

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

    public String getFlagCode() {
        return flagCode;
    }

    public boolean isTradable() {
        return tradable;
    }

    public String getQuoteUpdated() {
        return quoteUpdated;
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

    public String getPreliminaryFeesType() {
        return preliminaryFeesType;
    }

    public String getTickerSymbol() {
        return tickerSymbol;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
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

    public double getPriceFiveYearsAgo() {
        return priceFiveYearsAgo;
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

    public String getAssetRootCategory() {
        return assetRootCategory;
    }

    public String getAssetSubCategory() {
        return assetSubCategory;
    }

    public String getAssetSubSubCategory() {
        return assetSubSubCategory;
    }

    public String getIssuerName() {
        return issuerName;
    }

    public String getProspectus() {
        return prospectus;
    }

    public double getManagementFee() {
        return managementFee;
    }

    public double getLeverage() {
        return leverage;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getDirection() {
        return direction;
    }
}
