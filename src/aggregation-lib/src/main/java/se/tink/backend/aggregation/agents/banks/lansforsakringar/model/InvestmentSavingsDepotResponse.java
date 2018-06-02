package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InvestmentSavingsDepotResponse {
    private InvestmentSavingsDepotEntity investmentSavingsDepotWrapper;

    public InvestmentSavingsDepotEntity getInvestmentSavingsDepotWrapper() {
        return investmentSavingsDepotWrapper;
    }

    public void setInvestmentSavingsDepotWrapper(
            InvestmentSavingsDepotEntity investmentSavingsDepotWrapper) {
        this.investmentSavingsDepotWrapper = investmentSavingsDepotWrapper;
    }
}
