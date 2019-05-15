package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentPayloadEntity {

    private String iban;
    private String bban;
    private String pan;
    private String maskedPan;
    private String msisdn;
    private String currency;

    public ConsentPayloadEntity(
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
