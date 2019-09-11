package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.executor.payment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OriginAccount {
    private String iban;

    public OriginAccount(String iban) {
        this.iban = iban;
    }
}
