package se.tink.backend.aggregation.agents.banks.nordea.v20.model.authentication;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import se.tink.backend.aggregation.agents.banks.nordea.NordeaHashMapDeserializer;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthenticationToken {

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String token;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String profileId;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String tokenMaxAge;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String sessionMaxLength;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String notAfter;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String loginTime;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String authLevel;

    public String getAuthLevel() {
        return authLevel;
    }

    public void setAuthLevel(String authLevel) {
        this.authLevel = authLevel;
    }

    public String getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(String loginTime) {
        this.loginTime = loginTime;
    }

    public String getNotAfter() {
        return notAfter;
    }

    public void setNotAfter(String notAfter) {
        this.notAfter = notAfter;
    }

    public String getSessionMaxLength() {
        return sessionMaxLength;
    }

    public void setSessionMaxLength(String sessionMaxLength) {
        this.sessionMaxLength = sessionMaxLength;
    }

    public String getTokenMaxAge() {
        return tokenMaxAge;
    }

    public void setTokenMaxAge(String tokenMaxAge) {
        this.tokenMaxAge = tokenMaxAge;
    }

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
