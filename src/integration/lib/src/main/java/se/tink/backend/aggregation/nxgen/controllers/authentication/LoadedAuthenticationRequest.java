package se.tink.backend.aggregation.nxgen.controllers.authentication;

import com.google.common.collect.ImmutableList;
import se.tink.backend.agents.rpc.Credentials;

/** AuthenticationRequest decorator -- includes all data the agent needs to initiate a request. */
public final class LoadedAuthenticationRequest implements AuthenticationRequest {
    private final AuthenticationRequest request;
    private final Credentials credentials;

    public LoadedAuthenticationRequest(
            final AuthenticationRequest request, final Credentials credentials) {
        this.request = request;
        this.credentials = credentials;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    @Override
    public String getStep() {
        return request.getStep();
    }

    @Override
    public ImmutableList<String> getUserInputs() {
        return request.getUserInputs();
    }
}
