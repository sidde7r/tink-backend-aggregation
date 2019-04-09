package se.tink.backend.aggregation.agents.banks.nordea.v15.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthenticationToken {
    protected Map<String, Object> token = new HashMap<String, Object>();
    protected Map<String, Object> authLevel = new HashMap<String, Object>();
    protected Map<String, Object> loginTime = new HashMap<String, Object>();
    protected Map<String, Object> notAfter = new HashMap<String, Object>();
    protected Map<String, Object> sessionMaxLength = new HashMap<String, Object>();
    protected Map<String, Object> tokenMaxAge = new HashMap<String, Object>();
    protected Map<String, Object> profileId = new HashMap<String, Object>();

    public Map<String, Object> getAuthLevel() {
        return authLevel;
    }

    public void setAuthLevel(Map<String, Object> authLevel) {
        this.authLevel = authLevel;
    }

    public Map<String, Object> getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(Map<String, Object> loginTime) {
        this.loginTime = loginTime;
    }

    public Map<String, Object> getNotAfter() {
        return notAfter;
    }

    public void setNotAfter(Map<String, Object> notAfter) {
        this.notAfter = notAfter;
    }

    public Map<String, Object> getSessionMaxLength() {
        return sessionMaxLength;
    }

    public void setSessionMaxLength(Map<String, Object> sessionMaxLength) {
        this.sessionMaxLength = sessionMaxLength;
    }

    public Map<String, Object> getTokenMaxAge() {
        return tokenMaxAge;
    }

    public void setTokenMaxAge(Map<String, Object> tokenMaxAge) {
        this.tokenMaxAge = tokenMaxAge;
    }

    public Map<String, Object> getProfileId() {
        return profileId;
    }

    public void setProfileId(Map<String, Object> profileId) {
        this.profileId = profileId;
    }

    public Map<String, Object> getToken() {
        return token;
    }

    public void setToken(Map<String, Object> token) {
        this.token = token;
    }
}
