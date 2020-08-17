package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.investment.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.AmountEntity;

public class TypedAmountEntity extends AmountEntity {
    private String type;

    public String getType() {
        return type;
    }
}
