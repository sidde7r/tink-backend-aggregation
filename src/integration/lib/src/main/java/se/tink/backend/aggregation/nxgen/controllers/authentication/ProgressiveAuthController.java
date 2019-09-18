package se.tink.backend.aggregation.nxgen.controllers.authentication;

import com.google.common.collect.Lists;
import java.util.LinkedList;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;

public final class ProgressiveAuthController {

    private final ProgressiveAuthenticator authenticator;
    private final Credentials credentials;

    private ProgressiveAuthController(
            final ProgressiveAuthenticator authenticator, final Credentials credentials) {
        this.authenticator = authenticator;
        this.credentials = credentials;
    }

    public static ProgressiveAuthController of(
            final ProgressiveAuthenticator authenticator, final Credentials credentials) {
        return new ProgressiveAuthController(authenticator, credentials);
    }

    public SteppableAuthenticationResponse login(SteppableAuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {

        LinkedList<? extends AuthenticationStep> authSteps =
                Lists.newLinkedList(authenticator.authenticationSteps());
        return executeStep(authSteps, determineStepClassToExecute(authSteps, request), request);
    }

    private Class<? extends AuthenticationStep> determineStepClassToExecute(
            LinkedList<? extends AuthenticationStep> authenticationSteps,
            SteppableAuthenticationRequest request) {
        return request.getStep().orElse(authenticationSteps.getFirst().getClass());
    }

    private SteppableAuthenticationResponse executeStep(
            LinkedList<? extends AuthenticationStep> authenticationSteps,
            final Class<? extends AuthenticationStep> stepClassToExecute,
            final SteppableAuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        final AuthenticationRequest loadedRequest =
                request.getPayload().withCredentials(credentials);
        AuthenticationStep stepToExecute =
                authenticationSteps.stream()
                        .filter(step -> stepClassToExecute.isInstance(step))
                        .findAny()
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "The agent seems to have defined no steps"));
        if (isLastStep(stepToExecute, authenticationSteps)) {
            return SteppableAuthenticationResponse.finalResponse(
                    stepToExecute.respond(loadedRequest));
        } else {
            final AuthenticationStep upcomingStep =
                    authenticationSteps.get(authenticationSteps.indexOf(stepToExecute) + 1);
            return SteppableAuthenticationResponse.intermediateResponse(
                    upcomingStep.getClass(), stepToExecute.respond(loadedRequest));
        }
    }

    private boolean isLastStep(
            final AuthenticationStep step,
            LinkedList<? extends AuthenticationStep> authenticationSteps) {
        return authenticationSteps.getLast().equals(step);
    }
}
