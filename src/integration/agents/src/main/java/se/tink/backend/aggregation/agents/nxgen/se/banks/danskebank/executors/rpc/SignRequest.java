package se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@Builder
@JsonObject
public class SignRequest {

    @JsonProperty("package")
    private String reference;

    private String channel;

    private String mode;
}
