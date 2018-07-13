package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BorrowerEntity {
    private String name;
    private String debtOwnershipShare;
    private String interestRateOwnershipShare;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDebtOwnershipShare() {
        return debtOwnershipShare;
    }

    public void setDebtOwnershipShare(String debtOwnershipShare) {
        this.debtOwnershipShare = debtOwnershipShare;
    }

    public String getInterestRateOwnershipShare() {
        return interestRateOwnershipShare;
    }

    public void setInterestRateOwnershipShare(String interestRateOwnershipShare) {
        this.interestRateOwnershipShare = interestRateOwnershipShare;
    }
}
