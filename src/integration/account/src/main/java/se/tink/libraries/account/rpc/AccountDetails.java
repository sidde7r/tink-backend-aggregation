package se.tink.libraries.account.rpc;

import com.google.common.base.MoreObjects;
import java.util.Date;

public class AccountDetails {
    private Double interest;
    private Integer numMonthsBound;
    private String type;
    private Date nextDayOfTermsChange;

    public AccountDetails() {}

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

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("interest", interest == null ? null : "***")
                .add("numMonthsBound", numMonthsBound)
                .add("type", type)
                .add("nextDayOfTermsChange", nextDayOfTermsChange)
                .toString();
    }
}
