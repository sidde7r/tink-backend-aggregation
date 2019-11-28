package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto;

import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.TypeValuePair;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InvestmentPlanInvestmentDistributionDto {

    private TypeValuePair name;
    private TypeValuePair percentage;

    public TypeValuePair getName() {
        return name;
    }

    public TypeValuePair getPercentage() {
        return percentage;
    }
}
