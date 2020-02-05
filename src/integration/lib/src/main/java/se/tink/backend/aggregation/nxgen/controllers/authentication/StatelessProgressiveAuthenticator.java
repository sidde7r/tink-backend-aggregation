package se.tink.backend.aggregation.nxgen.controllers.authentication;

import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AuthenticationControllerType;

public abstract class StatelessProgressiveAuthenticator implements AuthenticationControllerType {

    private AuthenticationStep currentStep;

    public SteppableAuthenticationResponse processAuthentication(
            final SteppableAuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        Optional<AuthenticationStep> stepToExecute = determineStepToExecute(request);
        while (stepToExecute.isPresent()) {
            currentStep = stepToExecute.get();
            AuthenticationStepResponse stepResponse = executeStep(request);
            if (stepResponse.isAuthenticationFinished()) {
                break;
            } else if (stepResponse.getSupplementInformationRequester().isPresent()) {
                return SteppableAuthenticationResponse.intermediateResponse(
                        currentStep.getIdentifier(),
                        stepResponse.getSupplementInformationRequester().get());
            }
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
        List<? extends AuthenticationStep> authSteps = authenticationSteps();
        return authSteps.stream()
                .filter(s -> s.getIdentifier().equals(stepId))
                .findAny()
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Step with identifier [" + stepId + "] doesn't exist"));
    }

    private Optional<AuthenticationStep> getStepNextToCurrent() {
        List<? extends AuthenticationStep> authSteps = authenticationSteps();
        int currentStepId = authSteps.indexOf(currentStep);
        if (currentStepId < authSteps.size() - 1) {
            return Optional.of(authSteps.get(currentStepId + 1));
        }
        return Optional.empty();
    }

    public abstract List<? extends AuthenticationStep> authenticationSteps();
}
