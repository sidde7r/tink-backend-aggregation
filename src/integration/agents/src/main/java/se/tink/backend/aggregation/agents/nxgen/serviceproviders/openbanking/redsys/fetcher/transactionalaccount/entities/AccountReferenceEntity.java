package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountReferenceEntity {
    @JsonProperty private String iban;
    @JsonProperty private String bban;
    @JsonProperty private String pan;
    @JsonProperty private String maskedPan;
    @JsonProperty private String msisdn;
    @JsonProperty private String currency;
}
