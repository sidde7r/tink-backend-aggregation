package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BankdataCardIdEntity {
    private String cardNumber;
    private String maskedCardNumber;

    public String getCardNumber() {
        return cardNumber;
    }

    public String getMaskedCardNumber() {
        return maskedCardNumber;
    }
}
