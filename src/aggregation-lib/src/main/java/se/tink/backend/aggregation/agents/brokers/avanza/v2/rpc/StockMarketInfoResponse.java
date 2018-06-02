package se.tink.backend.aggregation.agents.brokers.avanza.v2.rpc;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import se.tink.backend.aggregation.agents.brokers.avanza.v2.model.CompanyEntity;
import se.tink.backend.aggregation.agents.brokers.avanza.v2.model.DividendEntity;
import se.tink.backend.aggregation.agents.brokers.avanza.v2.model.KeyRatiosEntity;
import se.tink.backend.aggregation.agents.brokers.avanza.v2.model.LatestTradeEntity;
import se.tink.backend.aggregation.agents.brokers.avanza.v2.model.OrderDepthLevelEntity;
import se.tink.backend.aggregation.agents.brokers.avanza.v2.model.PositionEntity;
import se.tink.backend.aggregation.agents.brokers.avanza.v2.model.RelatedStockEntity;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class StockMarketInfoResponse {
    private double priceOneWeekAgo;
    private double priceOneMonthAgo;
    private double priceSixMonthsAgo;
    private double priceAtStartOfYear;
    private double priceOneYearAgo;
    private double priceThreeYearsAgo;
    private double priceFiveYearsAgo;
    private double priceThreeMonthsAgo;
    private String marketPlace;
    private String marketList;
    private String currency;
    private String morningStarFactSheetUrl;
    private String name;
    private String id;
    private String country;
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
    private boolean shortSellable;
    private String tickerSymbol;
    private String flagCode;
    private double loanFactor;
    private String quoteUpdated;
    private KeyRatiosEntity keyRatios;
    private long numberOfOwners;
    private boolean superLoan;
    private Integer numberOfPriceAlerts;
    private boolean pushPermitted;
    private List<DividendEntity> dividends;
    private List<RelatedStockEntity> relatedStocks;
    private CompanyEntity company;
    private List<OrderDepthLevelEntity> orderDepthLevels;
    private boolean marketMakerExpected;
    private String orderDepthReceivedTime;
    private List<LatestTradeEntity> latestTrades;
    private boolean marketTrades;
    private List<PositionEntity> positions;
    private double positionsTotalValue;

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

    public String getMarketPlace() {
        return marketPlace;
    }

    public String getMarketList() {
        return marketList;
    }

    public String getCurrency() {
        return currency;
    }

    public String getMorningStarFactSheetUrl() {
        return morningStarFactSheetUrl;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getCountry() {
        return country;
    }

    public boolean getTradable() {
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

    public boolean getShortSellable() {
        return shortSellable;
    }

    public String getTickerSymbol() {
        return tickerSymbol;
    }

    public String getFlagCode() {
        return flagCode;
    }

    public double getLoanFactor() {
        return loanFactor;
    }

    public String getQuoteUpdated() {
        return quoteUpdated;
    }

    public KeyRatiosEntity getKeyRatios() {
        return keyRatios;
    }

    public long getNumberOfOwners() {
        return numberOfOwners;
    }

    public boolean getSuperLoan() {
        return superLoan;
    }

    public int getNumberOfPriceAlerts() {
        return numberOfPriceAlerts;
    }

    public boolean getPushPermitted() {
        return pushPermitted;
    }

    public List<DividendEntity> getDividends() {
        return dividends;
    }

    public List<RelatedStockEntity> getRelatedStocks() {
        return relatedStocks;
    }

    public CompanyEntity getCompany() {
        return company;
    }

    public List<OrderDepthLevelEntity> getOrderDepthLevels() {
        return orderDepthLevels;
    }

    public boolean getMarketMakerExpected() {
        return marketMakerExpected;
    }

    public String getOrderDepthReceivedTime() {
        return orderDepthReceivedTime;
    }

    public List<LatestTradeEntity> getLatestTrades() {
        return latestTrades;
    }

    public boolean getMarketTrades() {
        return marketTrades;
    }

    public List<PositionEntity> getPositions() {
        return positions;
    }

    public double getPositionsTotalValue() {
        return positionsTotalValue;
    }
}
