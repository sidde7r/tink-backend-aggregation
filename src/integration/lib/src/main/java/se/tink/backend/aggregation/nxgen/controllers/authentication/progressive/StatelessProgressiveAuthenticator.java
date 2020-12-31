package se.tink.backend.aggregation.nxgen.controllers.authentication.progressive;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;

public abstract class StatelessProgressiveAuthenticator {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private AuthenticationStep currentStep;

    public SteppableAuthenticationResponse processAuthentication(
            final SteppableAuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        Optional<AuthenticationStep> stepToExecute = determineStepToExecute(request);
        while (stepToExecute.isPresent()) {
            currentStep = stepToExecute.get();
            logger.info(
                    "Authentication flow state: Executing step with id "
                            + currentStep.getIdentifier());
            AuthenticationStepResponse stepResponse = executeStep(request);
            if (stepResponse.isAuthenticationFinished()) {
                break;
            } else if (stepResponse.getSupplementInformationRequester().isPresent()) {
                logger.info("Authentication flow state: Asking for supplement information");
                return SteppableAuthenticationResponse.intermediateResponse(
                        currentStep.getIdentifier(),
                        stepResponse.getSupplementInformationRequester().get());
            }
            logger.info("Authentication flow state: Finalizing authentication");
            stepToExecute = determineStepToExecute(stepResponse);
        }
        return SteppableAuthenticationResponse.finalResponse();
    }

    private AuthenticationStepResponse executeStep(SteppableAuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        AuthenticationStepResponse response = currentStep.execute(request.getPayload());
        request.clearManualStepCallbackData();
        return response;
    }

    private Optional<AuthenticationStep> determineStepToExecute(
            SteppableAuthenticationRequest steppableAuthenticationRequest) {
        if (steppableAuthenticationRequest.getStepIdentifier().isPresent()) {
            return Optional.of(
                    getStepById(steppableAuthenticationRequest.getStepIdentifier().get()));
        } else {
            return authenticationSteps().isEmpty()
                    ? Optional.empty()
                    : Optional.of(authenticationSteps().get(0));
        }
    }

    private Optional<AuthenticationStep> determineStepToExecute(
            AuthenticationStepResponse response) {
        if (response.getNextStepId().isPresent()) {
            return Optional.of(getStepById(response.getNextStepId().get()));
        } else {
            return getStepNextToCurrent();
        }
    }

    private AuthenticationStep getStepById(String stepId) {
        List<AuthenticationStep> authSteps = authenticationSteps();
        return authSteps.stream()
                .filter(s -> s.getIdentifier().equals(stepId))
                .findAny()
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Step with identifier [" + stepId + "] doesn't exist"));
    }

    private Optional<AuthenticationStep> getStepNextToCurrent() {
        List<AuthenticationStep> authSteps = authenticationSteps();
        int currentStepId = authSteps.indexOf(currentStep);
        if (currentStepId < authSteps.size() - 1) {
            return Optional.of(authSteps.get(currentStepId + 1));
        }
        return Optional.empty();
    }

    public abstract List<AuthenticationStep> authenticationSteps();
}
