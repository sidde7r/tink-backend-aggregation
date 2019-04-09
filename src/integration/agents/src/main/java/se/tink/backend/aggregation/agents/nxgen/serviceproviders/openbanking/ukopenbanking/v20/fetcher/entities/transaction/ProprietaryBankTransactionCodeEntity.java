package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v20.fetcher.entities.transaction;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ProprietaryBankTransactionCodeEntity {

    @JsonProperty("Code")
    private String code;

    @JsonProperty("Issuer")
    private String issuer;
}
