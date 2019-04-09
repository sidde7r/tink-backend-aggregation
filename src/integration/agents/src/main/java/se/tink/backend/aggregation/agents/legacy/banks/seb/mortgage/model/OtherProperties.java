package se.tink.backend.aggregation.agents.banks.seb.mortgage.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

public class OtherProperties {
    private Double assessmentValueOther;
    private Double loanAmountOther;
    private Double marketValueOther;
    private Double monthlyFeeOther;
    private String propertyTypeOther;
    private Double yearlyFeeOther;

    @JsonProperty("assessment_value_other")
    public Double getAssessmentValueOther() {
        return assessmentValueOther;
    }

    public void setAssessmentValueOther(Double assessmentValueOther) {
        this.assessmentValueOther = assessmentValueOther;
    }

    @JsonProperty("loan_amount_other")
    public Double getLoanAmountOther() {
        return loanAmountOther;
    }

    public void setLoanAmountOther(Double loanAmountOther) {
        this.loanAmountOther = loanAmountOther;
    }

    @JsonProperty("market_value_other")
    public Double getMarketValueOther() {
        return marketValueOther;
    }

    public void setMarketValueOther(Double marketValueOther) {
        this.marketValueOther = marketValueOther;
    }

    @JsonProperty("monthly_fee_other")
    public Double getMonthlyFeeOther() {
        return monthlyFeeOther;
    }

    public void setMonthlyFeeOther(Double monthlyFeeOther) {
        this.monthlyFeeOther = monthlyFeeOther;
    }

    @JsonProperty("property_type_other")
    public String getPropertyTypeOther() {
        return propertyTypeOther;
    }

    public void setPropertyTypeOther(String propertyTypeOther) {
        this.propertyTypeOther = propertyTypeOther;
    }

    @JsonProperty("yearly_fee_other")
    public Double getYearlyFeeOther() {
        return yearlyFeeOther;
    }

    public void setYearlyFeeOther(Double yearlyFeeOther) {
        this.yearlyFeeOther = yearlyFeeOther;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(
                assessmentValueOther,
                loanAmountOther,
                marketValueOther,
                monthlyFeeOther,
                propertyTypeOther,
                yearlyFeeOther);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        OtherProperties that = (OtherProperties) o;

        return Objects.equal(this.assessmentValueOther, that.assessmentValueOther)
                && Objects.equal(this.loanAmountOther, that.loanAmountOther)
                && Objects.equal(this.marketValueOther, that.marketValueOther)
                && Objects.equal(this.monthlyFeeOther, that.monthlyFeeOther)
                && Objects.equal(this.propertyTypeOther, that.propertyTypeOther)
                && Objects.equal(this.yearlyFeeOther, that.yearlyFeeOther);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("assessmentValueOther", assessmentValueOther)
                .add("loanAmountOther", loanAmountOther)
                .add("marketValueOther", marketValueOther)
                .add("monthlyFeeOther", monthlyFeeOther)
                .add("propertyTypeOther", propertyTypeOther)
                .add("yearlyFeeOther", yearlyFeeOther)
                .toString();
    }
}
