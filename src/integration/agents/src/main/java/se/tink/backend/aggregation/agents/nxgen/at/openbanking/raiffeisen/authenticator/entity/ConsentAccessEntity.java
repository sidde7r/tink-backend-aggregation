package se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.authenticator.entity;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentAccessEntity {

    private List<ConsentPayloadEntity> balances;
    private List<ConsentPayloadEntity> transactions;

    public ConsentAccessEntity(
            List<ConsentPayloadEntity> balances, List<ConsentPayloadEntity> transactions) {
        this.balances = balances;
        this.transactions = transactions;
    }
}
