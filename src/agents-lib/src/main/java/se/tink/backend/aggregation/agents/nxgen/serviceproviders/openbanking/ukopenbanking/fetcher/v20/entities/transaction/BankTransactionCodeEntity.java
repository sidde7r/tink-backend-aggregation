package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher.v20.entities.transaction;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BankTransactionCodeEntity {
    @JsonProperty("Code")
    private String code;
    @JsonProperty("SubCode")
    private String subCode;
}
