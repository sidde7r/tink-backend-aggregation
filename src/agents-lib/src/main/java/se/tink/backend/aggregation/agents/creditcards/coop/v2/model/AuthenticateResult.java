package se.tink.backend.aggregation.agents.creditcards.coop.v2.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthenticateResult {
    @JsonProperty("MemberAccountType")
    private int memberAccountType;
    @JsonProperty("Token")
    private String token;
    @JsonProperty("UserID")
    private long userId;
    @JsonProperty("UserSummary")
    private UserSummaryEntity userSummary;

    public int getMemberAccountType() {
        return memberAccountType;
    }

    public void setMemberAccountType(int memberAccountType) {
        this.memberAccountType = memberAccountType;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public UserSummaryEntity getUserSummary() {
        return userSummary;
    }

    public void setUserSummary(UserSummaryEntity userSummary) {
        this.userSummary = userSummary;
    }
}
