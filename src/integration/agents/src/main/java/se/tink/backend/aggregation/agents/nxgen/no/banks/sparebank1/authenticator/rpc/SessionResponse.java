package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class SessionResponse {

    @JsonProperty("m2")
    private String serverEvidenceMessage;
}
