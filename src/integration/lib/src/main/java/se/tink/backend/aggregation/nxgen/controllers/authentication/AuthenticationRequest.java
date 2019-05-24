package se.tink.backend.aggregation.nxgen.controllers.authentication;

import com.google.common.collect.ImmutableList;
import java.util.List;
import se.tink.backend.agents.rpc.Credentials;

/** Includes all data the authentication step needs in order to make an authentication response. */
public final class AuthenticationRequest implements Credentialsable {
    private final ImmutableList<String> userInputs;
    private final Credentials credentials;

    public AuthenticationRequest(final List<String> userInputs, final Credentials credentials) {
        this.userInputs = ImmutableList.copyOf(userInputs);
        this.credentials = credentials;
    }

    @Override
    public Credentials getCredentials() {
        return credentials;
    }

    public ImmutableList<String> getUserInputs() {
        return userInputs;
    }
}
