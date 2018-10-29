package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LogoutResponse extends BaseResponse {
    @JsonProperty("logged_out")
    boolean loggedOut;

    public boolean isLoggedOut() {
        return loggedOut;
    }
}