package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto;

import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.TypeValuePair;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InvestmentPlanPositionDto {

    private TypeValuePair productName;
    private TypeValuePair amount;
    private TypeValuePair currency;

    public TypeValuePair getProductName() {
        return productName;
    }

    public TypeValuePair getAmount() {
        return amount;
    }

    public TypeValuePair getCurrency() {
        return currency;
    }
}
