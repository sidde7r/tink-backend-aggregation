package se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ArtifactResponse {
    private String country;
    private String lang;

    @JsonProperty("logged_in")
    private boolean loggedIn;

    @JsonProperty("session_type")
    private String sessionType;

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public String getSessionType() {
        return sessionType;
    }
}
