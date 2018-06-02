package se.tink.backend.aggregation.agents.utils.authentication.encap.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ActivationResultEntity {
    private String signingKeyPhrase;
    private RegistrationEntity registration;
    private String b64OtpChallenge;
    private String b64ClientSaltNextKey;
    private int clientSaltNextKeyId;
    private PluginEntity plugin;

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

    public PluginEntity getPlugin() {
        return plugin;
    }
}
