package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.card.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardAccountEntity {

    private String maskedPan;

    public String getMaskedPan() {
        return maskedPan;
    }
}
