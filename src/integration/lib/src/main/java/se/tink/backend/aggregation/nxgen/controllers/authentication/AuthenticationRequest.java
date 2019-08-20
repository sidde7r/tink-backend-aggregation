package se.tink.backend.aggregation.nxgen.controllers.authentication;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import se.tink.backend.agents.rpc.Credentials;

/** Includes all data the authentication step needs in order to make an authentication response. */
public final class AuthenticationRequest implements Credentialsable {

    private final ImmutableList<String> userInputs;
    private final Credentials credentials;
    private ImmutableMap<String, String> callbackData;

    public AuthenticationRequest(final List<String> userInputs, final Credentials credentials) {
        this.userInputs = ImmutableList.copyOf(userInputs);
        this.credentials = credentials;
    }

    public static AuthenticationRequest createEmpty() {
        return new AuthenticationRequest(Collections.emptyList(), null);
    }

    public static AuthenticationRequest fromCallbackData(final Map<String, String> callbackData) {
        AuthenticationRequest request = new AuthenticationRequest(Collections.emptyList(), null);
        request.callbackData = ImmutableMap.copyOf(callbackData);
        return request;
    }

    @Override
    public Credentials getCredentials() {
        return credentials;
    }

    public ImmutableList<String> getUserInputs() {
        return userInputs;
    }

    public ImmutableMap<String, String> getCallbackData() {
        return callbackData;
    }

    public AuthenticationRequest withCredentials(final Credentials newCredentials) {
        final AuthenticationRequest request = new AuthenticationRequest(userInputs, newCredentials);
        request.callbackData = callbackData;
        return request;
    }
}
