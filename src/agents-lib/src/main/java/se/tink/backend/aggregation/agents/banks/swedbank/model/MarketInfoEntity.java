package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MarketInfoEntity {
    private String name;
    private String description;
    private String category;
    private AmountEntity rate;
    private String startDate;
    private String fundCode;
    private String rateDate;
    private boolean fundguide;
    private RatingEntity rating;
    private String tsid;
    private String isincode;
    private String legalResidenceCountryCode;
    private String ppcode;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public AmountEntity getRate() {
        return rate;
    }

    public void setRate(AmountEntity rate) {
        this.rate = rate;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getFundCode() {
        return fundCode;
    }

    public void setFundCode(String fundCode) {
        this.fundCode = fundCode;
    }

    public String getRateDate() {
        return rateDate;
    }

    public void setRateDate(String rateDate) {
        this.rateDate = rateDate;
    }

    public boolean isFundguide() {
        return fundguide;
    }

    public void setFundguide(boolean fundguide) {
        this.fundguide = fundguide;
    }

    public RatingEntity getRating() {
        return rating;
    }

    public void setRating(RatingEntity rating) {
        this.rating = rating;
    }

    public String getTsid() {
        return tsid;
    }

    public void setTsid(String tsid) {
        this.tsid = tsid;
    }

    public String getIsincode() {
        return isincode;
    }

    public void setIsincode(String isincode) {
        this.isincode = isincode;
    }

    public String getLegalResidenceCountryCode() {
        return legalResidenceCountryCode;
    }

    public void setLegalResidenceCountryCode(String legalResidenceCountryCode) {
        this.legalResidenceCountryCode = legalResidenceCountryCode;
    }

    public String getPpcode() {
        return ppcode;
    }

    public void setPpcode(String ppcode) {
        this.ppcode = ppcode;
    }
}
