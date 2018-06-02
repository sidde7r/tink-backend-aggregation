package se.tink.backend.aggregation.rpc;

import java.util.Date;
import se.tink.backend.core.Loan;

public class AccountDetails {
    private Double interest;
    private Integer numMonthsBound;
    private String type;
    private Date nextDayOfTermsChange;

    public AccountDetails() {
    }

    public AccountDetails(Loan loan) {
        interest = loan.getInterest();
        numMonthsBound = loan.getNumMonthsBound();
        type = loan.getType() != null ? loan.getType().toString() : null;
        nextDayOfTermsChange = loan.getNextDayOfTermsChange();
    }

    public Double getInterest() {
        return interest;
    }

    public void setInterest(Double interest) {
        this.interest = interest;
    }

    public Integer getNumMonthsBound() {
        return numMonthsBound;
    }

    public void setNumMonthsBound(Integer numMonthsBound) {
        this.numMonthsBound = numMonthsBound;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getNextDayOfTermsChange() {
        return nextDayOfTermsChange;
    }

    public void setNextDayOfTermsChange(Date nextDayOfTermsChange) {
        this.nextDayOfTermsChange = nextDayOfTermsChange;
    }
}
