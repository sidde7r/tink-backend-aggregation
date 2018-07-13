package se.tink.backend.aggregation.agents.nxgen.be.banks.bnppf.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionContextDetail {
    private String atmLocation;
    private String cardNumber;

    public String getAtmLocation() {
        return atmLocation;
    }

    public String getCardNumber() {
        return cardNumber;
    }
}
