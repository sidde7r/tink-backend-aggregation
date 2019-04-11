package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities.CurrentUser;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class IsAuthenticatedResponse extends BaseResponse {
    boolean authenticated;

    @JsonProperty("user_id")
    int userId;

    @JsonProperty("current_user")
    CurrentUser currentUser;

    public boolean isAuthenticated() {
        return authenticated;
    }

    @JsonIgnore
    public Optional<Integer> getUserId() {
        return Optional.ofNullable(userId);
    }

    public Optional<CurrentUser> getCurrentUser() {
        return Optional.ofNullable(currentUser);
    }
}
