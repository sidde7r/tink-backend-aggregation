package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities.CurrentUser;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class IsAuthenticatedResponse extends BaseResponse{
    boolean authenticated;
    @JsonProperty("user_id")
    int userId;
    String language;
    @JsonProperty("current_user")
    CurrentUser currentUser;

    public boolean isAuthenticated() {
        return authenticated;
    }

    public int getUserId() {
        return userId;
    }

    public String getLanguage() {
        return language;
    }

    public CurrentUser getCurrentUser() {
        return currentUser;
    }
}
