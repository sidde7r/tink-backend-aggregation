package se.tink.backend.aggregation.agents.banks.sbab.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoanTermsEntity {

    @JsonProperty("amorteringPerManad")
    private int amortizationPerMonth;

    // Note: this seems to be the same value as 'amorteringPerManad', with the only difference that
    // it is a double.
    private double amortizationAmount;

    @JsonProperty("amorteringsfritt")
    private boolean freeFromAmortization;

    private String amortizationType;

    // Example: MONTHS_1
    @JsonProperty("aviseringsperiod")
    private String notificationPeriod;

    // Example: MONTHS_3
    private String fixedInterestPeriodMonths;

    @JsonProperty("rantejusteringsdag")
    private long interestRateAdjustmentDate;

    @JsonProperty("ranterabatt")
    private double interestRateDiscount;

    private double interestRate;

    @JsonProperty("villkorsandringsdag")
    private long nextDayOfTermsChange;

    public int getAmortizationPerMonth() {
        return amortizationPerMonth;
    }

    public void setAmortizationPerMonth(int amortizationPerMonth) {
        this.amortizationPerMonth = amortizationPerMonth;
    }

    public double getAmortizationValue() {
        return amortizationAmount;
    }

    public void setAmortizationValue(double amortizationValue) {
        this.amortizationAmount = amortizationValue;
    }

    public boolean isFreeFromAmortization() {
        return freeFromAmortization;
    }

    public void setFreeFromAmortization(boolean freeFromAmortization) {
        this.freeFromAmortization = freeFromAmortization;
    }

    public String getAmortizationType() {
        return amortizationType;
    }

    public void setAmortizationType(String amortizationType) {
        this.amortizationType = amortizationType;
    }

    public String getNotificationPeriod() {
        return notificationPeriod;
    }

    public void setNotificationPeriod(String notificationPeriod) {
        this.notificationPeriod = notificationPeriod;
    }

    public String getInterestRateBoundPeriod() {
        return fixedInterestPeriodMonths;
    }

    public void setInterestRateBoundPeriod(String interestRateBoundPeriod) {
        this.fixedInterestPeriodMonths = interestRateBoundPeriod;
    }

    public long getInterestRateAdjustmentDate() {
        return interestRateAdjustmentDate;
    }

    public void setInterestRateAdjustmentDate(long interestRateAdjustmentDate) {
        this.interestRateAdjustmentDate = interestRateAdjustmentDate;
    }

    public double getInterestRateDiscount() {
        return interestRateDiscount;
    }

    public void setInterestRateDiscount(double interestRateDiscount) {
        this.interestRateDiscount = interestRateDiscount;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public double getNormalizedInterestRate() {
        BigDecimal interest = BigDecimal.valueOf(interestRate);
        return interest.doubleValue();
    }

    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
    }

    public long getNextDayOfTermsChange() {
        return nextDayOfTermsChange;
    }

    public void setNextDayOfTermsChange(long nextDayOfTermsChange) {
        this.nextDayOfTermsChange = nextDayOfTermsChange;
    }
}
