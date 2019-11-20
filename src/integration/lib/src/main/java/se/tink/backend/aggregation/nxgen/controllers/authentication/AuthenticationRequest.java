package se.tink.backend.aggregation.nxgen.controllers.authentication;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import se.tink.backend.agents.rpc.Credentials;

/** Includes all data the authentication step needs in order to make an authentication response. */
public final class AuthenticationRequest implements Credentialsable {

    private Credentials credentials;
    private ImmutableMap<String, String> userInputs;
    private ImmutableMap<String, String> callbackData;

    public AuthenticationRequest(final Credentials credentials) {
        this.credentials = credentials;
    }

    public AuthenticationRequest withCallbackData(final Map<String, String> callbackData) {
        this.callbackData = ImmutableMap.copyOf(callbackData);
        return this;
    }

    public AuthenticationRequest withUserInputs(final Map<String, String> userInputs) {
        this.userInputs = ImmutableMap.copyOf(userInputs);
        return this;
    }

    @Override
    public Credentials getCredentials() {
        return credentials;
    }

    public ImmutableMap<String, String> getUserInputs() {
        return userInputs;
    }

    @Deprecated
    public ImmutableList<String> getUserInputsAsList() {
        return ImmutableList.copyOf(userInputs.values());
    }

    public ImmutableMap<String, String> getCallbackData() {
        return callbackData;
    }

    @Deprecated
    public AuthenticationRequest withCredentials(final Credentials credentials) {
        this.credentials = credentials;
        return this;
    }
}
