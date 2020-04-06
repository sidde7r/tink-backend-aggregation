package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.entities.InvestmentSavingsDepotWrapperEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchISKResponse {
    private InvestmentSavingsDepotWrapperEntity investmentSavingsDepotWrapper;

    public InvestmentSavingsDepotWrapperEntity getInvestmentSavingsDepotWrapper() {
        return investmentSavingsDepotWrapper;
    }
}
