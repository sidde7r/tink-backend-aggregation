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

    public Integer getDecimalPlaces() {
        return decimalPlaces;
    }

    public BigDecimal getLast() {
        return last;
    }
}
