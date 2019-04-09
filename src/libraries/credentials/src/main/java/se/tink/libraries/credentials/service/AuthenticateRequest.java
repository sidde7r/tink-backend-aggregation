package se.tink.libraries.credentials.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Set;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
import se.tink.libraries.user.rpc.User;

public class AuthenticateRequest extends CredentialsRequest {
    @JsonProperty
    private boolean manual;

    public AuthenticateRequest() {

    }

    public AuthenticateRequest(User user, Provider provider, Credentials credentials, boolean manual, boolean create, boolean update) {
        super(user, provider, credentials);

        this.manual = manual;
        this.create = create;
        this.update = update;

        this.getCredentials().setForceManualAuthentication(true);
    }

    public AuthenticateRequest(User user, Provider provider, Credentials credentials, boolean manual) {
        this(user, provider, credentials, manual, false, false);
    }

    @Override
    public CredentialsRequestType getType() {
        return CredentialsRequestType.REFRESH_INFORMATION;
    }

    public void setManual(boolean manual) {
        this.manual = manual;
    }

    @Override
    public boolean isManual() {
        return manual;
    }
}
