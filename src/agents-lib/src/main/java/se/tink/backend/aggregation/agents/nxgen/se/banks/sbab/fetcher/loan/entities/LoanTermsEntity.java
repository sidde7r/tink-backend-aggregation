package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.loan.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoanTermsEntity {
    @JsonProperty("payment_frequency_months")
    private String paymentFrequencyMonths;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("condition_rollover_date")
    private Date conditionRolloverDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("interest_rate_adjustment_date")
    private Date interestRateAdjustmentDate;

    @JsonProperty("interest_rate")
    private double interestRate;

    @JsonProperty("binding_time_months")
    private int bindingTimeMonths;

    @JsonProperty("amortisation_amount")
    private long amortisationAmount;

    public String getPaymentFrequencyMonths() {
        return paymentFrequencyMonths;
    }

    public Date getConditionRolloverDate() {
        return conditionRolloverDate;
    }

    public Date getInterestRateAdjustmentDate() {
        return interestRateAdjustmentDate;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public int getBindingTimeMonths() {
        return bindingTimeMonths;
    }

    public long getAmortisationAmount() {
        return amortisationAmount;
    }
}
