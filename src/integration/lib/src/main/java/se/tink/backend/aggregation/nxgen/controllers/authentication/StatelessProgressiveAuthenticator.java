package se.tink.backend.aggregation.nxgen.controllers.authentication;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AuthenticationControllerType;

public abstract class StatelessProgressiveAuthenticator implements AuthenticationControllerType {

    public SteppableAuthenticationResponse processAuthentication(
            final SteppableAuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        List<? extends AuthenticationStep> stepsToProcess = findAndExtractStepsToProcess(request);
        for (AuthenticationStep step : stepsToProcess) {
            Optional<SupplementInformationRequester> response = step.execute(request.getPayload());
            request.clearManualStepCallbackData();
            if (response.isPresent()) {
                return SteppableAuthenticationResponse.intermediateResponse(
                        step.getIdentifier(), response.get());
            }
        }
        return SteppableAuthenticationResponse.finalResponse();
    }

    private List<? extends AuthenticationStep> findAndExtractStepsToProcess(
            final SteppableAuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        List<? extends AuthenticationStep> authSteps = Lists.newArrayList(authenticationSteps());
        if (request.getStepIdentifier().isPresent()) {
            for (int i = 0; i < authSteps.size(); i++) {
                if (authSteps.get(i).getIdentifier().equals(request.getStepIdentifier().get())) {
                    return authSteps.subList(i, authSteps.size());
                }
            }
            throw new IllegalStateException(
                    "Step with identifier ["
                            + request.getStepIdentifier().get()
                            + "] doesn't exist");
        } else {
            return authSteps;
        }
    }

    public abstract Iterable<? extends AuthenticationStep> authenticationSteps()
            throws AuthenticationException, AuthorizationException;
}
