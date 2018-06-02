package se.tink.backend.aggregation.agents.banks.se.icabanken.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InterestsEntity {
    private double accrued;
    private double interest;

    public double getAccrued() {
        return accrued;
    }

    public void setAccrued(double accrued) {
        this.accrued = accrued;
    }

    public double getInterest() {
        return interest;
    }

    public void setInterest(double interest) {
        this.interest = interest;
    }
}
