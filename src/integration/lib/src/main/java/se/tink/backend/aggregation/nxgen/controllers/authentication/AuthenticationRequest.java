package se.tink.backend.aggregation.nxgen.controllers.authentication;

import java.util.List;

/**
 * In progressive authentication, carry request information such as step, userInputs and credential.
 */
public interface AuthenticationRequest {
    String getStep();

    List<String> getUserInputs();
}
