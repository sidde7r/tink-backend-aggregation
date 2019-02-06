package se.tink.backend.aggregation.agents.utils.authentication.encap.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthenticationResultEntity {
    private int clientSaltCurrentKeyId;
    private String b64ClientSaltCurrentKey;
    private int purpose;
    private int totalAttempts;
    private int remainingAttempts;
    private Boolean authenticationWithoutPin;
    private List<String> allowedAuthMethods;
    private String b64OtpChallenge;
    private String b64ClientSaltNextKey;
    private int clientSaltNextKeyId;
    private PluginEntity plugin;

    public int getClientSaltCurrentKeyId() {
        return clientSaltCurrentKeyId;
    }

    public String getB64ClientSaltCurrentKey() {
        return b64ClientSaltCurrentKey;
    }

    public int getPurpose() {
        return purpose;
    }

    public int getTotalAttempts() {
        return totalAttempts;
    }

    public int getRemainingAttempts() {
        return remainingAttempts;
    }

    public Boolean getAuthenticationWithoutPin() {
        return authenticationWithoutPin;
    }

    public List<String> getAllowedAuthMethods() {
        return allowedAuthMethods;
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

    public PluginEntity getPlugin() {
        return plugin;
    }
}
