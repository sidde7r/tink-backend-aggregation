package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.authenticator.entities.LoginResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@JsonObject
public class CustomerLoginResponse {

    @JsonProperty("CustomerLoginResponse")
    private LoginResponse loginResponse;

    public LoginResponse getLoginResponse() {
        return loginResponse;
    }
}
