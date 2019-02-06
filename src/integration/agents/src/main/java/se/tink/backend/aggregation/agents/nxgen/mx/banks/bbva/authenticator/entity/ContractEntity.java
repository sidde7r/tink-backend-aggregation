package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ContractEntity {
    private NumberTypeEntity numberType;
    private String number;

    public ContractEntity(String cardNumber) {
        this.number = cardNumber;
        this.numberType = new NumberTypeEntity();
    }
}
