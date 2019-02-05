package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BankIdInitRequest {
    private String identificationNumber;

    public BankIdInitRequest(String identificationNumber) {
        this.identificationNumber = identificationNumber;
    }

    public String getIdentificationNumber() {
        return identificationNumber;
    }

    public void setIdentificationNumber(String identificationNumber) {
        this.identificationNumber = identificationNumber;
    }
}
