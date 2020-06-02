package se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.authenticator.rpc.entities.BankIdErrorEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BankIdInitResponse {

    private String orderRef;

    private String autoStartToken;

    private String collectUrl;

    private String country;

    private String environment;

    private boolean loggedIn;

    private String sessionId;

    private String lang;

    @JsonProperty("session_type")
    private String sessionType;

    @JsonProperty("expires_in")
    private int expiresIn;

    private BankIdErrorEntity error;

    public String getCountry() {
        return country;
    }

    public String getEnvironment() {
        return environment;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getSessionType() {
        return sessionType;
    }

    public String getLang() {
        return lang;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public String getOrderRef() {
        return orderRef;
    }

    public String getAutoStartToken() {
        return autoStartToken;
    }

    public String getCollectUrl() {
        return collectUrl;
    }

    public BankIdErrorEntity getError() {
        return error;
    }
}
