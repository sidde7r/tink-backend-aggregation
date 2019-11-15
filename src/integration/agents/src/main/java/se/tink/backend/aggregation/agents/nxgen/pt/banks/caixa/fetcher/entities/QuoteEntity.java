package se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.entities;

import java.math.BigDecimal;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class QuoteEntity {

    private Long absoluteVariation;
    private BigDecimal ask;
    private Long availableAmount;
    private Long availableQuantity;
    private Long bid;
    private Long change;
    private BigDecimal close;
    private String currency;
    private Integer decimalPlaces;
    private Long high;
    private String isin;
    private BigDecimal last;
    private Long low;
    private Long open;
    private String priceParametrization;
    private Integer pricePrecision;
    private Integer quantityDecimalPlaces;
    private Date quoteDateTime;
    private Date quoteEndTime;
    private Date quoteStartTime;
    private Boolean realtimeValues;
    private String reutersId;
    private Long riskClass;
    private Long securitiesId;
    private String securitiesName;
    private Long volume;
    private Long yearHigh;
    private Long yearLow;
    private MarketEntity market;
    private AssetType assetType;

    public Long getAbsoluteVariation() {
        return absoluteVariation;
    }

    public BigDecimal getAsk() {
        return ask;
    }

    public Long getAvailableAmount() {
        return availableAmount;
    }

    public Long getAvailableQuantity() {
        return availableQuantity;
    }

    public Long getBid() {
        return bid;
    }

    public Long getChange() {
        return change;
    }

    public BigDecimal getClose() {
        return close;
    }

    public String getCurrency() {
        return currency;
    }

    public Integer getDecimalPlaces() {
        return decimalPlaces;
    }

    public Long getHigh() {
        return high;
    }

    public String getIsin() {
        return isin;
    }

    public BigDecimal getLast() {
        return last;
    }

    public Long getLow() {
        return low;
    }

    public Long getOpen() {
        return open;
    }

    public String getPriceParametrization() {
        return priceParametrization;
    }

    public Integer getPricePrecision() {
        return pricePrecision;
    }

    public Integer getQuantityDecimalPlaces() {
        return quantityDecimalPlaces;
    }

    public Date getQuoteDateTime() {
        return quoteDateTime;
    }

    public Date getQuoteEndTime() {
        return quoteEndTime;
    }

    public Date getQuoteStartTime() {
        return quoteStartTime;
    }

    public Boolean getRealtimeValues() {
        return realtimeValues;
    }

    public String getReutersId() {
        return reutersId;
    }

    public Long getRiskClass() {
        return riskClass;
    }

    public Long getSecuritiesId() {
        return securitiesId;
    }

    public String getSecuritiesName() {
        return securitiesName;
    }

    public Long getVolume() {
        return volume;
    }
}
