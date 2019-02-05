package se.tink.backend.aggregation.agents.nxgen.se.creditcards.coop.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.coop.rpc.UserSummaryResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthenticateResultEntity {
    @JsonProperty("MemberAccountType")
    private int memberAccountType;
    @JsonProperty("Token")
    private String token;
    @JsonProperty("UserID")
    private int userId;
    @JsonProperty("UserSummary")
    private UserSummaryResponse userSummary;

    public String getToken() {
        return token;
    }

    public int getUserId() {
        return userId;
    }

    public UserSummaryResponse getUserSummary() {
        return userSummary;
    }
}
