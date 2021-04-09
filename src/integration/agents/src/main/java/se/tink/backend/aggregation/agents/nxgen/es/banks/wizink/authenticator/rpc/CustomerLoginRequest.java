package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CustomerLoginRequest {

    @JsonProperty("CustomerLoginRequest")
    private RequestBody requestBody;

    public CustomerLoginRequest(String userId, String password) {
        this.requestBody = new RequestBody(userId, password);
    }

    @JsonObject
    private static class RequestBody {
        private String userId;
        private String password;

        private RequestBody(String userId, String password) {
            this.userId = userId;
            this.password = password;
        }
    }
}
