package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@Setter
@JsonObject
public class EasyPinProvisionResponse {

    @JsonProperty("X-ENROLMENT-STATUS-CODE")
    private String status;

    @JsonProperty("X-ENROLMENT-MESSAGE")
    private String message;

    @JsonProperty("X-ENROLMENT-ENC-CREDENTIALS")
    private String encCredentials;
}
