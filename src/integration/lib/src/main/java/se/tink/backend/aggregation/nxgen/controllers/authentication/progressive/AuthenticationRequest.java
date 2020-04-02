package se.tink.backend.aggregation.nxgen.controllers.authentication.progressive;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Objects;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Credentialsable;

/** Includes all data the authentication step needs in order to make an authentication response. */
public final class AuthenticationRequest implements Credentialsable {

    private Credentials credentials;
    private ImmutableMap<String, String> userInputs = ImmutableMap.of();
    private ImmutableMap<String, String> callbackData = ImmutableMap.of();

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AuthenticationRequest that = (AuthenticationRequest) o;
        return Objects.equals(credentials, that.credentials)
                && Objects.equals(userInputs, that.userInputs)
                && Objects.equals(callbackData, that.callbackData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(credentials, userInputs, callbackData);
    }

    void clearCallbackDataAndInputs() {
        callbackData = ImmutableMap.of();
        userInputs = ImmutableMap.of();
    }
}
