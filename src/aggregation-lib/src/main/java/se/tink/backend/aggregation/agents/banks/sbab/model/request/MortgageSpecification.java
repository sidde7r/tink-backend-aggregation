package se.tink.backend.aggregation.agents.banks.sbab.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.libraries.application.GenericApplicationFieldGroup;
import se.tink.backend.core.enums.ApplicationFieldName;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MortgageSpecification {

    // The mortgage amount (required).
    @JsonProperty("belopp")
    private Integer amount;

    // The purpose of the mortgage (required).
    @JsonProperty("syfte")
    private MortgagePurpose purpose;

    // The user's desired date for the mortgage payout ('yyyy-mm-dd', not required).
    @JsonProperty("onskadUtbetalningsdag")
    private String desiredPayoutDate;

    // Information about the deposit (not required).
    @JsonProperty("handpenning")
    private MortgageDeposit desposit;

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public String getPurpose() {
        return purpose.toString();
    }

    public void setPurpose(MortgagePurpose purpose) {
        this.purpose = purpose;
    }

    public String getDesiredPayoutDate() {
        return desiredPayoutDate;
    }

    public void setDesiredPayoutDate(String desiredPayoutDate) {
        this.desiredPayoutDate = desiredPayoutDate;
    }

    public MortgageDeposit getDesposit() {
        return desposit;
    }

    public void setDesposit(MortgageDeposit desposit) {
        this.desposit = desposit;
    }

    public static MortgageSpecification createFromApplication(GenericApplicationFieldGroup currentMortgageGroup) {
        MortgageSpecification specification = new MortgageSpecification();
        specification.setAmount(currentMortgageGroup.getFieldAsInteger(ApplicationFieldName.AMOUNT));
        specification.setPurpose(MortgagePurpose.OMLAGGNING);
        return specification;
    }
}
