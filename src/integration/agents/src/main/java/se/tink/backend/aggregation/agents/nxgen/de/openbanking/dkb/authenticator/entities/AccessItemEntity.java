package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccessItemEntity {
    private String iban;
    private String bban;
    private String pan;
    private String maskedPan;
    private String msisdn;
    private String currency;

    public AccessItemEntity(
            String iban,
            String bban,
            String pan,
            String maskedPan,
            String msisdn,
            String currency) {
        this.iban = iban;
        this.bban = bban;
        this.pan = pan;
        this.maskedPan = maskedPan;
        this.msisdn = msisdn;
        this.currency = currency;
    }
}
