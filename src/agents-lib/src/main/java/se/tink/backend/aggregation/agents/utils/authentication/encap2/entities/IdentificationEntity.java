package se.tink.backend.aggregation.agents.utils.authentication.encap2.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class IdentificationEntity {
    private int purpose;
    private boolean authenticationWithoutPin;
    private List<Object> allowedAuthMethods;
    private int clientSaltCurrentKeyId;
    private String b64ClientSaltCurrentKey;
    private long lastAttempt;
    private int totalAttempts;
    private int remainingAttempts;
    private String b64OtpChallenge;
    private String b64ClientSaltNextKey;
    private int clientSaltNextKeyId;
    private Object plugin;

    public int getPurpose() {
        return purpose;
    }

    public int getClientSaltCurrentKeyId() {
        return clientSaltCurrentKeyId;
    }

    public String getB64ClientSaltCurrentKey() {
        return b64ClientSaltCurrentKey;
    }

    public long getLastAttempt() {
        return lastAttempt;
    }

    public int getTotalAttempts() {
        return totalAttempts;
    }

    public int getRemainingAttempts() {
        return remainingAttempts;
    }

    public String getB64OtpChallenge() {
        return b64OtpChallenge;
    }

    public String getB64ClientSaltNextKey() {
        return b64ClientSaltNextKey;
    }

    public int getClientSaltNextKeyId() {
        return clientSaltNextKeyId;
    }
}
