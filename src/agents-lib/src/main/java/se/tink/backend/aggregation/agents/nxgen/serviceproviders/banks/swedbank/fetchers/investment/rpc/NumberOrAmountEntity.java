package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.investment.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NumberOrAmountEntity {
    private String type;
    private AmountEntity nominalValue;

    public String getType() {
        return type;
    }

    public AmountEntity getNominalValue() {
        return nominalValue;
    }
}
