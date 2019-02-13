package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.rpc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.entities.PositionEntity;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.entities.UnderlyingOrderbookEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CertificateMarketInfoResponse {
    private String marketPlace;
    private int numberOfPriceAlerts;
    private boolean pushPermitted;
    private String currency;
    private String name;
    private String id;
    private boolean tradable;
    private double buyPrice;
    private double lastPrice;
    private String lastPriceUpdated;
    private double change;
    private double changePercent;
    private long totalVolumeTraded;
    private long totalValueTraded;
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
    private UnderlyingOrderbookEntity underlyingOrderbook;
    private List<PositionEntity> positions;
    private double positionsTotalValue;
    private String shortName;
    private double administrationFee;
    private double leverage;
    private String underlyingCurrency;
    private String endDate;
    private String direction;
    private String issuerName;
    private String prospectus;
    private String assetRootCategory;
    private String assetSubCategory;
    private String assetSubSubCategory;

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

    public long getTotalValueTraded() {
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

    public UnderlyingOrderbookEntity getUnderlyingOrderbook() {
        return underlyingOrderbook;
    }

    public List<PositionEntity> getPositions() {
        return Optional.ofNullable(positions).orElse(Collections.emptyList());
    }

    public double getPositionsTotalValue() {
        return positionsTotalValue;
    }

    public String getShortName() {
        return shortName;
    }

    public double getAdministrationFee() {
        return administrationFee;
    }

    public double getLeverage() {
        return leverage;
    }

    public String getUnderlyingCurrency() {
        return underlyingCurrency;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getDirection() {
        return direction;
    }

    public String getIssuerName() {
        return issuerName;
    }

    public String getProspectus() {
        return prospectus;
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
}
