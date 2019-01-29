package se.tink.libraries.account.rpc;

import io.protostuff.Tag;
import java.util.Date;

public class AccountDetails {

    @Tag(1)
    private Double interest;
    @Tag(2)
    private Integer numMonthsBound;
    @Tag(3)
    private String type;
    @Tag(4)
    private Date nextDayOfTermsChange;
    
    public AccountDetails() {
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
