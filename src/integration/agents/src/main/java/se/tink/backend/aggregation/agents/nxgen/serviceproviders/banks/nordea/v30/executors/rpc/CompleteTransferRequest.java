package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.executors.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Builder
public class CompleteTransferRequest {
    @JsonProperty("code")
    private String code;

    @JsonProperty("signing_type")
    @Builder.Default
    private String signingType = "nasa";
}
