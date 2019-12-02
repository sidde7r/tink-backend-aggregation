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

    private AuthenticationRequest() {}

    public static AuthenticationRequest empty() {
        return new AuthenticationRequest();
    }

    public static AuthenticationRequest fromCallbackData(final Map<String, String> callbackData) {
        AuthenticationRequest request = new AuthenticationRequest();
        request.callbackData = ImmutableMap.copyOf(callbackData);
        return request;
    }

    public static AuthenticationRequest fromUserInputs(final Map<String, String> userInputs) {
        final AuthenticationRequest request = new AuthenticationRequest();
        request.userInputs = ImmutableMap.copyOf(userInputs);
        return request;
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

    public AuthenticationRequest withCredentials(final Credentials newCredentials) {
        final AuthenticationRequest request = new AuthenticationRequest();
        request.userInputs = userInputs;
        request.callbackData = callbackData;
        request.credentials = newCredentials;
        return request;
    }
}
