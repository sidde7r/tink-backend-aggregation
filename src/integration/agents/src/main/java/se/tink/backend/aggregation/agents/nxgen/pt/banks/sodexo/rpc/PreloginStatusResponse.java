package se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@JsonObject
public class PreloginStatusResponse {

    @JsonProperty("num-messages")
    private String numMessage;

    @JsonProperty("new-user-token")
    private String newUserToken;

    @JsonProperty("allow-prelogin-balance")
    private String allowPreloginBalance;
}
