package se.tink.backend.aggregation.nxgen.controllers.authentication;

import java.util.List;
import se.tink.backend.agents.rpc.Credentials;

/**
 * In progressive authentication, carry request information such as step, userInputs and credential.
 */
public class AuthenticationRequest {
    private String step;
    private List<String> userInputs;
    private Credentials credentials;

    public AuthenticationRequest(String step, List<String> userInputs) {
        this.step = step;
        this.userInputs = userInputs;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    public String getStep() {
        return step;
    }

    public List<String> getUserInputs() {
        return userInputs;
    }
}
