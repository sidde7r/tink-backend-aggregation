package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DescriptiveAmountEntity extends AmountEntity {
    private String description;

    public String getDescription() {
        return description;
    }
}
