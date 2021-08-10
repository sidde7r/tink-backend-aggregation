package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
public class AccountReferenceEntity {
    @JsonProperty private String iban;
    @JsonProperty private String bban;
    @JsonProperty private String pan;
    @JsonProperty private String maskedPan;
    @JsonProperty private String msisdn;
    @JsonProperty private String currency;

    public static AccountReferenceEntity ofIban(String iban) {
        AccountReferenceEntity entity = new AccountReferenceEntity();
        entity.iban = iban;
        return entity;
    }
}
