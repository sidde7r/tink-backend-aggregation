package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentPayloadEntity {

    private String iban;

    public ConsentPayloadEntity(String iban) {
        this.iban = iban;
    }
}
