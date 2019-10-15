package se.tink.backend.aggregation.agents.utils.authentication.encap3.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class IdentificationEntity {
    @JsonProperty private int purpose;
    @JsonProperty private boolean authenticationWithoutPin;
    @JsonProperty private List<Object> allowedAuthMethods;
    @JsonProperty private int clientSaltCurrentKeyId;
    @JsonProperty private String b64ClientSaltCurrentKey;
    @JsonProperty private long lastAttempt;
    @JsonProperty private int totalAttempts;
    @JsonProperty private int remainingAttempts;
    @JsonProperty private String b64OtpChallenge;
    @JsonProperty private String b64ClientSaltNextKey;
    @JsonProperty private int clientSaltNextKeyId;
    @JsonProperty private Object plugin;

    @JsonIgnore
    public int getPurpose() {
        return purpose;
    }

    @JsonIgnore
    public int getClientSaltCurrentKeyId() {
        return clientSaltCurrentKeyId;
    }

    @JsonIgnore
    public String getB64ClientSaltCurrentKey() {
        return b64ClientSaltCurrentKey;
    }

    @JsonIgnore
    public long getLastAttempt() {
        return lastAttempt;
    }

    @JsonIgnore
    public int getTotalAttempts() {
        return totalAttempts;
    }

    @JsonIgnore
    public int getRemainingAttempts() {
        return remainingAttempts;
    }

    @JsonIgnore
    public String getB64OtpChallenge() {
        return b64OtpChallenge;
    }

    @JsonIgnore
    public String getB64ClientSaltNextKey() {
        return b64ClientSaltNextKey;
    }

    @JsonIgnore
    public int getClientSaltNextKeyId() {
        return clientSaltNextKeyId;
    }
}
