package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.investment.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.AmountEntity;

public class TypedAmountEntity extends AmountEntity {
    private String type;

    public String getType() {
        return type;
    }
}
