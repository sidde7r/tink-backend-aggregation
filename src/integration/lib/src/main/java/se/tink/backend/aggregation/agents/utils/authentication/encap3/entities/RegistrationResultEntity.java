package se.tink.backend.aggregation.agents.utils.authentication.encap3.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RegistrationResultEntity {
    @JsonProperty private String signingKeyPhrase;
    @JsonProperty private RegistrationEntity registration;
    @JsonProperty private String b64OtpChallenge;
    @JsonProperty private String b64ClientSaltNextKey;
    @JsonProperty private int clientSaltNextKeyId;
    @JsonProperty private Object plugin;

    @JsonIgnore
    public String getSigningKeyPhrase() {
        return signingKeyPhrase;
    }

    @JsonIgnore
    public RegistrationEntity getRegistration() {
        return registration;
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
