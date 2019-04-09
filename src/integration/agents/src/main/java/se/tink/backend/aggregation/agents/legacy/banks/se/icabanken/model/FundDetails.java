package se.tink.backend.aggregation.agents.banks.se.icabanken.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FundDetails {
    @JsonProperty("Name")
    private String name;

    @JsonProperty("Id")
    private String id;

    @JsonProperty("Active")
    private boolean active;

    @JsonProperty("Buyable")
    private boolean buyable;

    @JsonProperty("Currency")
    private CurrencyEntity currency; // "Code": "kr"

    @JsonProperty("CountryCodeTwoChars")
    private String countryCode;

    @JsonProperty("TradingCode")
    private String tradingCode; // SEK for swedish

    @JsonProperty("NetAssetValue")
    private double newAssetValue;

    @JsonProperty("NetAssetValueDate")
    private String netAssetValueDate;

    @JsonProperty("ISIN")
    private String isin;

    @JsonProperty("MorningstarId")
    private String morningstarId;

    @JsonProperty("Rating")
    private int rating;

    @JsonProperty("Risk")
    private int risk;

    @JsonProperty("Fees")
    private FeeEntity fees;

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

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isBuyable() {
        return buyable;
    }

    public void setBuyable(boolean buyable) {
        this.buyable = buyable;
    }

    public CurrencyEntity getCurrency() {
        return currency;
    }

    public void setCurrency(CurrencyEntity currency) {
        this.currency = currency;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getTradingCode() {
        return tradingCode;
    }

    public void setTradingCode(String tradingCode) {
        this.tradingCode = tradingCode;
    }

    public double getNewAssetValue() {
        return newAssetValue;
    }

    public void setNewAssetValue(double newAssetValue) {
        this.newAssetValue = newAssetValue;
    }

    public String getNetAssetValueDate() {
        return netAssetValueDate;
    }

    public void setNetAssetValueDate(String netAssetValueDate) {
        this.netAssetValueDate = netAssetValueDate;
    }

    public String getIsin() {
        return isin;
    }

    public void setIsin(String isin) {
        this.isin = isin;
    }

    public String getMorningstarId() {
        return morningstarId;
    }

    public void setMorningstarId(String morningstarId) {
        this.morningstarId = morningstarId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public int getRisk() {
        return risk;
    }

    public void setRisk(int risk) {
        this.risk = risk;
    }

    public FeeEntity getFees() {
        return fees;
    }

    public void setFees(FeeEntity fees) {
        this.fees = fees;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public DevelopmentEntity getDevelopment() {
        return development;
    }

    public void setDevelopment(DevelopmentEntity development) {
        this.development = development;
    }

    public String getMinimumBuyAmount() {
        return minimumBuyAmount;
    }

    public void setMinimumBuyAmount(String minimumBuyAmount) {
        this.minimumBuyAmount = minimumBuyAmount;
    }

    public int getSustainabilityRating() {
        return sustainabilityRating;
    }

    public void setSustainabilityRating(int sustainabilityRating) {
        this.sustainabilityRating = sustainabilityRating;
    }
}
