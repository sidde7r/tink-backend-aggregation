package se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@Setter
@JsonObject
public class SignRequest {

    @JsonProperty("package")
    private String reference;

    private String channel;

    private String mode;

    private SignRequest(String reference) {
        this.reference = reference;
        this.mode = "Sign";
        this.channel = "0";
    }

    public static SignRequest createFromReference(String reference) {
        return new SignRequest(reference);
    }
}
