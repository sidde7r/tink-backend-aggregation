package se.tink.backend.aggregation.agents.utils.authentication.encap2.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RegistrationResultEntity {
    private String signingKeyPhrase;
    private RegistrationEntity registration;
    private String b64OtpChallenge;
    private String b64ClientSaltNextKey;
    private int clientSaltNextKeyId;
    private Object plugin;

    public String getSigningKeyPhrase() {
        return signingKeyPhrase;
    }

    public RegistrationEntity getRegistration() {
        return registration;
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
