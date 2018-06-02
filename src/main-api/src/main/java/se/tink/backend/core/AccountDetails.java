package se.tink.backend.core;

import io.protostuff.Tag;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;

public class AccountDetails {

    @Tag(1)
    @ApiModelProperty(name = "interest", value="Interest of the account. Applicable for loans and savings accounts.")
    private Double interest;
    @Tag(2)
    @ApiModelProperty(name = "numMonthsBound", value="Populated if available. Describes how many months the interest rate is bound.")
    private Integer numMonthsBound;
    @Tag(3)
    @ApiModelProperty(name = "type", value="Account subtype. Values: " + Loan.Type.DOCUMENTED)
    private String type;
    @Tag(4)
    @ApiModelProperty(name = "nextDayOfTermsChange", value="A timestamp of the next day of terms change of the account. Applicable for loans.")
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
