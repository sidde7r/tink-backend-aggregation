package se.tink.backend.aggregation.agents.brokers.nordnet.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InstrumentEntity {
    @JsonProperty("price_type")
    private String priceType;

    private List<TradableEntity> tradables;

    @JsonProperty("instrument_id")
    private String id;

    @JsonProperty("instrument_type")
    private String type;

    @JsonProperty("instrument_group_type")
    private String groupType;

    private String currency;

    @JsonProperty("number_of_securities")
    private double numberOfSecurities;

    private double multiplier;

    @JsonProperty("pawn_percentage")
    private double pawnPercentage;

    private String symbol;

    @JsonProperty("isin_code")
    private String isin;

    private String sector;

    @JsonProperty("sector_group")
    private String sectorGroup;

    private String name;

    public String getPriceType() {
        return priceType;
    }

    public void setPriceType(String priceType) {
        this.priceType = priceType;
    }

    public List<TradableEntity> getTradables() {
        return tradables;
    }

    public void setTradables(List<TradableEntity> tradables) {
        this.tradables = tradables;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getGroupType() {
        return groupType;
    }

    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public double getNumberOfSecurities() {
        return numberOfSecurities;
    }

    public void setNumberOfSecurities(double numberOfSecurities) {
        this.numberOfSecurities = numberOfSecurities;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    public double getPawnPercentage() {
        return pawnPercentage;
    }

    public void setPawnPercentage(double pawnPercentage) {
        this.pawnPercentage = pawnPercentage;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getIsin() {
        return isin;
    }

    public void setIsin(String isin) {
        this.isin = isin;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public String getSectorGroup() {
        return sectorGroup;
    }

    public void setSectorGroup(String sectorGroup) {
        this.sectorGroup = sectorGroup;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
