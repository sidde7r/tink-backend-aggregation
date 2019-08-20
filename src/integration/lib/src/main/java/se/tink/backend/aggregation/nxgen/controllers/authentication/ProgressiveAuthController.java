package se.tink.backend.aggregation.nxgen.controllers.authentication;

import java.util.Iterator;
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

        final AuthenticationRequest loadedRequest =
                new AuthenticationRequest(request.getPayload().getUserInputs(), credentials);

        final Iterator<? extends AuthenticationStep> steps =
                authenticator.authenticationSteps(loadedRequest.getCredentials()).iterator();

        if (!request.getStep().isPresent()) {
            final AuthenticationStep step = steps.next();
            if (steps.hasNext()) {
                final AuthenticationStep upcomingStep = steps.next();
                return SteppableAuthenticationResponse.intermediateResponse(
                        upcomingStep.getClass(), step.respond(loadedRequest));
            } else {
                return SteppableAuthenticationResponse.finalResponse(step.respond(loadedRequest));
            }
        }

        final Class<? extends AuthenticationStep> cls = request.getStep().get();

        while (steps.hasNext()) {
            final AuthenticationStep step = steps.next();
            if (cls.isInstance(step)) {
                if (steps.hasNext()) {
                    final AuthenticationStep upcomingStep = steps.next();
                    return SteppableAuthenticationResponse.intermediateResponse(
                            upcomingStep.getClass(), step.respond(loadedRequest));
                } else {
                    return SteppableAuthenticationResponse.finalResponse(
                            step.respond(loadedRequest));
                }
            }
        }
        throw new IllegalStateException("The agent seems to have defined no steps");
    }
}
