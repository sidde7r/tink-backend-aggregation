package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.HeaderResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InvestmentPlansOverviewResponse extends HeaderResponse {

    private List<InvestmentPlanDto> investmentPlans;

    public List<InvestmentPlanDto> getInvestmentPlans() {
        return investmentPlans;
    }
}
