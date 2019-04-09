package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Optional;
import se.tink.backend.aggregation.agents.models.Instrument;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FundEntity {
    @JsonIgnore private static final String CURRENCY = "SEK";
    private long fundId;
    private String name;
    private String company;
    private int risk;
    private String riskText;
    private String type;
    private HoldingEntity holding;
    private Boolean favorite;
    private Boolean recommended;
    private Boolean monthlySavingsExists;
    private Boolean internal;
    private Boolean mostSold;

    public long getFundId() {
        return fundId;
    }

    public void setFundId(long fundId) {
        this.fundId = fundId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public int getRisk() {
        return risk;
    }

    public void setRisk(int risk) {
        this.risk = risk;
    }

    public String getRiskText() {
        return riskText;
    }

    public void setRiskText(String riskText) {
        this.riskText = riskText;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public HoldingEntity getHolding() {
        return holding;
    }

    public void setHolding(HoldingEntity holding) {
        this.holding = holding;
    }

    public Boolean getFavorite() {
        return favorite;
    }

    public void setFavorite(Boolean favorite) {
        this.favorite = favorite;
    }

    public Boolean getRecommended() {
        return recommended;
    }

    public void setRecommended(Boolean recommended) {
        this.recommended = recommended;
    }

    public Boolean getMonthlySavingsExists() {
        return monthlySavingsExists;
    }

    public void setMonthlySavingsExists(Boolean monthlySavingsExists) {
        this.monthlySavingsExists = monthlySavingsExists;
    }

    public Boolean getInternal() {
        return internal;
    }

    public void setInternal(Boolean internal) {
        this.internal = internal;
    }

    public Boolean getMostSold() {
        return mostSold;
    }

    public void setMostSold(Boolean mostSold) {
        this.mostSold = mostSold;
    }

    public Optional<Instrument> toInstrument(FundInformationEntity fundInformation) {
        HoldingEntity holding = getHolding();

        if (holding == null
                || holding.getNumberOfShares() == 0
                || holding.getPurchaseValue() == 0
                || fundInformation == null
                || fundInformation.getIsinCode() == null) {
            return Optional.empty();
        }

        Instrument instrument = new Instrument();

        instrument.setAverageAcquisitionPrice(
                holding.getPurchaseValue() / holding.getNumberOfShares());
        instrument.setCurrency(CURRENCY); // If LF adds a currency field, change this.
        instrument.setIsin(fundInformation.getIsinCode());
        instrument.setMarketPlace(fundInformation.getCompany());
        instrument.setMarketValue(holding.getTotalMarketValue());
        instrument.setName(getName());
        instrument.setPrice(holding.getTotalMarketValue() / holding.getNumberOfShares());
        instrument.setProfit(holding.getDevelopment());
        instrument.setQuantity(holding.getNumberOfShares());
        instrument.setRawType(getType());
        instrument.setType(Instrument.Type.FUND);
        instrument.setUniqueIdentifier(fundInformation.getIsinCode() + String.valueOf(getFundId()));

        return Optional.of(instrument);
    }
}
