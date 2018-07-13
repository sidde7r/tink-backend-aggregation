package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.investment.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AllocationEntity {
    private String type;
    private AmountEntity value;
    private String text;
    private String percent;

    public String getType() {
        return type;
    }

    public AmountEntity getValue() {
        return value;
    }

    public String getText() {
        return text;
    }

    public String getPercent() {
        return percent;
    }
}
