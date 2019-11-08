package se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.entities;

import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AssetEntity {

    private Integer accountingValuation;
    private BigDecimal accountingValuationCounterValue;
    private String assetISIN;
    private String assetId;
    private String assetName;
    private String assetNumber;
    private AssetType assetType;
    private BigDecimal availableAmount;
    private BigDecimal availableQuantity;
    private BigDecimal availableValuation;
    private BigDecimal availableValuationCounterValue;
    private BigDecimal averageBuyingUnitaryAmount;
    private String currency;
    private MarketEntity market;
    private Integer quantityDecimalPlaces;
    private BigDecimal quote;
    private String quoteDate;
    private Integer quoteDecimalPlaces;
    private BigDecimal settledAmount;
    private BigDecimal settledQuantity;
    private BigDecimal totalBuyingAmount;
    private String tradingLocation;

    public Integer getAccountingValuation() {
        return accountingValuation;
    }

    public String getAssetISIN() {
        return assetISIN;
    }

    public String getAssetId() {
        return assetId;
    }

    public String getAssetName() {
        return assetName;
    }

    public String getAssetNumber() {
        return assetNumber;
    }

    public AssetType getAssetType() {
        return assetType;
    }

    public BigDecimal getAvailableQuantity() {
        return availableQuantity;
    }

    public BigDecimal getAvailableValuation() {
        return availableValuation;
    }

    public String getCurrency() {
        return currency;
    }

    public Integer getQuantityDecimalPlaces() {
        return quantityDecimalPlaces;
    }

    public BigDecimal getQuote() {
        return quote;
    }

    public String getQuoteDate() {
        return quoteDate;
    }

    public String getTradingLocation() {
        return tradingLocation;
    }

    public MarketEntity getMarket() {
        return market;
    }

    public BigDecimal getAccountingValuationCounterValue() {
        return accountingValuationCounterValue;
    }

    public BigDecimal getAvailableAmount() {
        return availableAmount;
    }

    public BigDecimal getAvailableValuationCounterValue() {
        return availableValuationCounterValue;
    }

    public BigDecimal getAverageBuyingUnitaryAmount() {
        return averageBuyingUnitaryAmount;
    }

    public Integer getQuoteDecimalPlaces() {
        return quoteDecimalPlaces;
    }

    public BigDecimal getSettledAmount() {
        return settledAmount;
    }

    public BigDecimal getSettledQuantity() {
        return settledQuantity;
    }

    public BigDecimal getTotalBuyingAmount() {
        return totalBuyingAmount;
    }
}
