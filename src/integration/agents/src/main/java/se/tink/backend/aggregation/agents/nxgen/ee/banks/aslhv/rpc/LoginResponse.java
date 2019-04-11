package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.AsLhvConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginResponse extends BaseResponse {
    @JsonProperty("authentication")
    boolean authentication;

    @JsonProperty("user_id")
    int userId;

    @JsonIgnore
    public boolean isAuthenticated() {
        return authentication;
    }

    @JsonIgnore
    public int getUserId() {
        return userId;
    }

    @JsonIgnore
    public boolean incorrectCredentials() {
        boolean result = false;
        if (error != null && !error.isEmpty()) {
            if (error.get(0).equalsIgnoreCase(AsLhvConstants.Messages.INCORRECT_CREDENTIALS)) {
                result = true;
            }

            if (error.get(0).equalsIgnoreCase(AsLhvConstants.Messages.INVALID_PARAMETERS)) {
                if (errorFields != null || !errorFields.isEmpty()) {
                    if (errorFields.contains(AsLhvConstants.Form.PASSWORD_PARAMETER)
                            || errorFields.contains(AsLhvConstants.Form.USERNAME_PARAMETER)) {
                        result = true;
                    }
                }
            }
        }
        return result;
    }
}
