package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FundDetailsEntity {

    @JsonProperty("Name")
    private String name;
    @JsonProperty("Id")
    private String id;
    @JsonProperty("Active")
    private boolean active;
    @JsonProperty("Buyable")
    private boolean buyable;
    @JsonProperty("Currency")
    private CurrencyEntity currency;
    @JsonProperty("CountryCodeTwoChars")
    private String countryCodeTwoChars;
    @JsonProperty("TradingCode")
    private String tradingCode;
    @JsonProperty("NetAssetValue")
    private double netAssetValue;
    @JsonProperty("NetAssetValueDate")
    private String netAssetValueDate;
    @JsonProperty("HasMorningstarInfo")
    private boolean hasMorningstarInfo;
    @JsonProperty("MorningStarLink")
    private String morningStarLink;
    @JsonProperty("MorningstarFundPDFLink")
    private String morningstarFundPDFLink;
    @JsonProperty("MorningstarFundBrochurePDFLink")
    private String morningstarFundBrochurePDFLink;
    @JsonProperty("MorningstarFundGraphLink")
    private String morningstarFundGraphLink;
    @JsonProperty("MorningstarFundGraph410X228Link")
    private String morningstarFundGraph410X228Link;
    @JsonProperty("ISIN")
    private String iSIN;
    @JsonProperty("MorningstarId")
    private String morningstarId;
    @JsonProperty("Rating")
    private int rating;
    @JsonProperty("Risk")
    private int risk;
    @JsonProperty("Fees")
    private FeesEntity fees;
    @JsonProperty("Category")
    private String category;
    @JsonProperty("Development")
    private DevelopmentEntity development;
    @JsonProperty("MinimumBuyAmount")
    private String minimumBuyAmount;
    @JsonProperty("SustainabilityRating")
    private int sustainabilityRating;

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isBuyable() {
        return buyable;
    }

    public CurrencyEntity getCurrency() {
        return currency;
    }

    public String getCountryCodeTwoChars() {
        return countryCodeTwoChars;
    }

    public String getTradingCode() {
        return tradingCode;
    }

    public double getNetAssetValue() {
        return netAssetValue;
    }

    public String getNetAssetValueDate() {
        return netAssetValueDate;
    }

    public boolean isHasMorningstarInfo() {
        return hasMorningstarInfo;
    }

    public String getMorningStarLink() {
        return morningStarLink;
    }

    public String getMorningstarFundPDFLink() {
        return morningstarFundPDFLink;
    }

    public String getMorningstarFundBrochurePDFLink() {
        return morningstarFundBrochurePDFLink;
    }

    public String getMorningstarFundGraphLink() {
        return morningstarFundGraphLink;
    }

    public String getMorningstarFundGraph410X228Link() {
        return morningstarFundGraph410X228Link;
    }

    public String getiSIN() {
        return iSIN;
    }

    public String getMorningstarId() {
        return morningstarId;
    }

    public int getRating() {
        return rating;
    }

    public int getRisk() {
        return risk;
    }

    public FeesEntity getFees() {
        return fees;
    }

    public String getCategory() {
        return category;
    }

    public DevelopmentEntity getDevelopment() {
        return development;
    }

    public String getMinimumBuyAmount() {
        return minimumBuyAmount;
    }

    public int getSustainabilityRating() {
        return sustainabilityRating;
    }

}
