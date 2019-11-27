package se.tink.sa.agent.pt.ob.sibs.rest.client.authentication.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConsentPayloadEntity {

    private String iban;
    private String bban;
    private String pan;
    private String maskedPan;
    private String msisdn;
    private String currency;
}
