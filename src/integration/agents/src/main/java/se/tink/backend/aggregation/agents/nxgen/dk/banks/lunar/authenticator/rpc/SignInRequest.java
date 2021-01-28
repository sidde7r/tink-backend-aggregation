package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class SignInRequest {
    @JsonProperty("password")
    private final String lunarPassword;
}
