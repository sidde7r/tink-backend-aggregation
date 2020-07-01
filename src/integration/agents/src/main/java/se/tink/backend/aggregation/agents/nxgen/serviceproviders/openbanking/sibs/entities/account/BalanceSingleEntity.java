package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.entities.account;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalanceSingleEntity {

    private AmountEntity amount;

    public AmountEntity getAmount() {
        return amount;
    }
}
