package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties
public class FundInformationWrapper {
    private FundInformationEntity fundInformation;

    public FundInformationEntity getFundInformation() {
        return fundInformation;
    }

    public void setFundInformation(FundInformationEntity fundInformation) {
        this.fundInformation = fundInformation;
    }
}
