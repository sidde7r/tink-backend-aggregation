package se.tink.backend.aggregation.rpc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import java.util.Map;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;
import se.tink.libraries.user.rpc.User;

/**
 * Immutable credentials object that's supposed to be used only as hack when needing a credentials
 * instance, but we want to ensure we don't use it for supplemental information etc.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FakedCredentials extends Credentials {

    /**
     * These are the only properties usable on a FakedCredentials. If more are needed, put them
     * here. Though we should not use this credential to save stuff on since it's not supposed to be
     * persisted.
     */
    @JsonCreator
    public FakedCredentials(
            @JsonProperty("userId") String userId,
            @JsonProperty("userName") String userName,
            @JsonProperty("providerName") String providerName,
            @JsonProperty("type") CredentialsTypes type) {
        applySuperValues(userId, userName, providerName, type);
    }

    public FakedCredentials(User user, Provider provider) {
        applySuperValues(
                user.getId(),
                user.getUsername(),
                provider.getName(),
                provider.getCredentialsType());
    }

    private void applySuperValues(
            String userId, String userName, String providerName, CredentialsTypes type) {
        // Fake values that hopefully doesn't blow NPE's later on
        this.setType(type);
        super.setId(null);
        super.setStatus(null);
        super.setFieldsSerialized("{}");
        super.setSensitivePayloadSerialized("{}");

        // Link valid user info
        super.setUserId(userId);
        super.setUsername(userName);

        // Link valid provider info
        super.setProviderName(providerName);
    }

    /**
     * Original Credentials generates id if missing. This Credential has no id, so should not
     * generate.
     */
    @Override
    @JsonIgnore
    public String getId() {
        return null;
    }

    /*
     * We want to keep the type sent from the provider or serialized credential for providers to be able to differ auth
     * depending on the user setup or similar stuff.
     */
    @Override
    @JsonIgnore
    public void setType(CredentialsTypes type) {
        super.setType(type);
    }

    /*
     * Cannot throw for these fields since they are used throughout code. Also we do @JsonIgnore and use the
     * @JsonCreator above to specify exactly which fields are allowed to deserialize to.
     */
    @Override
    @JsonIgnore
    public void setField(String key, String value) {
        // Not saving on fake credential
    }

    @Override
    @JsonIgnore
    public void setField(Field.Key key, String value) {
        // Not saving on fake credential
    }

    @Override
    @JsonIgnore
    public void setFields(Map<String, String> fields) {
        // Not saving on fake credential
    }

    @Override
    @JsonIgnore
    public void setFieldsSerialized(String fieldsSerialized) {
        // Not saving on fake credential
    }

    @Override
    @JsonIgnore
    public void setSensitivePayload(String key, String value) {
        // Not saving on fake credential
    }

    @Override
    @JsonIgnore
    public void setSensitivePayload(Field.Key key, String value) {
        // Not saving on fake credential
    }

    @Override
    @JsonIgnore
    public void setSensitivePayloadAsMap(Map<String, String> sensitivePayload) {
        // Not saving on fake credential
    }

    @Override
    @JsonIgnore
    public void setSensitivePayloadSerialized(String sensitivePayloadSerialized) {
        // Not saving on fake credential
    }

    @Override
    @JsonIgnore
    public void setStatus(CredentialsStatus status) {
        // Not saving on fake credential
    }

    @Override
    @JsonIgnore
    public void setStatusPayload(String statusPayload) {
        // Not saving on fake credential
    }

    @Override
    @JsonIgnore
    public void setAdditionalInformation(String additionalInformation) {
        // Not saving on fake credential
    }

    @Override
    @JsonIgnore
    public void setDebugUntil(Date debugUntil) {
        // Not saving on fake credential
    }

    @Override
    @JsonIgnore
    public void setId(String id) {
        // Not saving on fake credential
    }

    @Override
    @JsonIgnore
    public void setNextUpdate(Date nextUpdate) {
        // Not saving on fake credential
    }

    @Override
    @JsonIgnore
    public void setPassword(String password) {
        // Not saving on fake credential
    }

    @Override
    @JsonIgnore
    public void setPayload(String payload) {
        // Not saving on fake credential
    }

    @Override
    @JsonIgnore
    public void setPersistentSession(Object object) {
        // Not saving on fake credential
    }

    @Override
    @JsonIgnore
    public void setProviderLatency(long providerLatency) {
        // Not saving on fake credential
    }

    @Override
    public void setProviderName(String provider) {
        // Not saving on fake credential
    }

    @Override
    @JsonIgnore
    public void setStatusPrompt(String statusPrompt) {
        // Not saving on fake credential
    }

    @Override
    @JsonIgnore
    public void setStatusUpdated(Date statusUpdated) {
        // Not saving on fake credential
    }

    @Override
    @JsonIgnore
    public void setSupplementalInformation(String supplementalInformation) {
        // Not saving on fake credential
    }

    @Override
    @JsonIgnore
    public void setUpdated(Date updated) {
        // Not saving on fake credential
    }

    @Override
    @JsonIgnore
    public void setUserId(String user) {
        // Not saving on fake credential
    }

    @Override
    @JsonIgnore
    public void setUsername(String username) {
        // Not saving on fake credential
    }
}
