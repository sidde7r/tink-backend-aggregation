package se.tink.backend.aggregation.nxgen.controllers.authentication.progressive;

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

        LinkedList<AuthenticationStep> authSteps =
                Lists.newLinkedList(authenticator.authenticationSteps());
        return executeStep(authSteps, determineStepClassToExecute(authSteps, request), request);
    }

    private String determineStepClassToExecute(
            LinkedList<AuthenticationStep> authenticationSteps,
            SteppableAuthenticationRequest request) {
        return request.getStepIdentifier().orElse(authenticationSteps.getFirst().getIdentifier());
    }

    private SteppableAuthenticationResponse executeStep(
            LinkedList<AuthenticationStep> authenticationSteps,
            final String stepIdentifier,
            final SteppableAuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        final AuthenticationRequest loadedRequest =
                request.getPayload().withCredentials(credentials);
        AuthenticationStep stepToExecute =
                authenticationSteps.stream()
                        .filter(step -> stepIdentifier.equals(step.getIdentifier()))
                        .findAny()
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "The agent seems to have defined no steps"));
        if (isLastStep(stepToExecute, authenticationSteps)) {
            stepToExecute.execute(loadedRequest);
            return SteppableAuthenticationResponse.finalResponse();
        } else {
            final AuthenticationStep upcomingStep =
                    authenticationSteps.get(authenticationSteps.indexOf(stepToExecute) + 1);
            return SteppableAuthenticationResponse.intermediateResponse(
                    upcomingStep.getIdentifier(),
                    stepToExecute.execute(loadedRequest).getSupplementInformationRequester().get());
        }
    }

    private static boolean isLastStep(
            final AuthenticationStep step, LinkedList<AuthenticationStep> authenticationSteps) {
        return authenticationSteps.getLast().equals(step);
    }
}
