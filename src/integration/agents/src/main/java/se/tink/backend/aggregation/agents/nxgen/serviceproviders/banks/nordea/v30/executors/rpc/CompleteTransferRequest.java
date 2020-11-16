package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.executors.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CompleteTransferRequest {
    @JsonProperty("code")
    private String code;

    @JsonProperty("signing_type")
    private String signingType = "nasa";

    public CompleteTransferRequest setCode(String code) {
        this.code = code;
        return this;
    }
}
