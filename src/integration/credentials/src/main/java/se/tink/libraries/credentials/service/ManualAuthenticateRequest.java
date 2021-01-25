package se.tink.libraries.credentials.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsRequestType;
import se.tink.libraries.user.rpc.User;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ManualAuthenticateRequest extends CredentialsRequest {
    @JsonProperty private boolean manual;

    public ManualAuthenticateRequest() {}

    public ManualAuthenticateRequest(
            User user,
            Provider provider,
            Credentials credentials,
            boolean manual,
            boolean create,
            boolean update) {
        super(user, provider, credentials);

        this.manual = manual;
        this.create = create;
        this.update = update;
    }

    public ManualAuthenticateRequest(
            User user, Provider provider, Credentials credentials, boolean manual) {
        this(user, provider, credentials, manual, false, false);
    }

    @Override
    public CredentialsRequestType getType() {
        return CredentialsRequestType.MANUAL_AUTHENTICATION;
    }

    public void setManual(boolean manual) {
        this.manual = manual;
    }

    @Override
    public boolean isManual() {
        return manual;
    }
}
