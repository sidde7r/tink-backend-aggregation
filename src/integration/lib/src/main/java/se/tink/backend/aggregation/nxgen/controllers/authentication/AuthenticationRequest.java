package se.tink.backend.aggregation.nxgen.controllers.authentication;

import com.google.common.collect.ImmutableList;

/**
 * In progressive authentication, carry request information such as step, userInputs and credential.
 */
public interface AuthenticationRequest {
    String getStep();

    ImmutableList<String> getUserInputs();
}
