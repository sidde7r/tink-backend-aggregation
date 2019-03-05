package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ContractValidateEntity {
    private String number;
    private NumberTypeEntity numberType;

    public ContractValidateEntity(String cardNumber) {
        this.number = cardNumber;
        this.numberType = new NumberTypeEntity();
    }
}
