package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication;

import java.util.HashMap;
import java.util.UUID;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentSucceededAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectPreparationRedirectUrlStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AgentError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.RefreshTokenFailureError;
import se.tink.backend.aggregation.agentsplatform.framework.error.Error;

final class KbcRedirectAuthenticationRefreshTokenStepHandler {

    AgentAuthenticationResult defineResultOfAccessTokenRefresh(
            AgentProceedNextStepAuthenticationRequest authenticationProcessRequest,
            AgentAuthenticationResult authenticationResult,
            boolean userAvailableForInteraction) {
        if (authenticationResult instanceof AgentSucceededAuthenticationResult) {

            return new AgentProceedNextStepAuthenticationResult(
                    AgentAuthenticationProcessStep.identifier(KbcConsentValidationStep.class),
                    authenticationProcessRequest.getAuthenticationPersistedData());
        } else if (nextStepIsRedirectStepAndUserIsNotAvailable(
                authenticationResult, userAvailableForInteraction)) {

            return new AgentFailedAuthenticationResult(
                    new RefreshTokenFailureError(
                            Error.builder()
                                    .uniqueId(UUID.randomUUID().toString())
                                    .errorMessage(SessionError.SESSION_EXPIRED.userMessage().get())
                                    .errorCode(AgentError.INVALID_CREDENTIALS.getCode())
                                    .build()),
                    new AgentAuthenticationPersistedData(new HashMap<>()));
        }

        return authenticationResult;
    }

    private boolean nextStepIsRedirectStepAndUserIsNotAvailable(
            AgentAuthenticationResult authenticationResult, boolean userAvailableForInteraction) {
        return authenticationResult instanceof AgentProceedNextStepAuthenticationResult
                && !userAvailableForInteraction
                && nextStepIsRedirectPreparationRedirectUrlStep(authenticationResult);
    }

    private boolean nextStepIsRedirectPreparationRedirectUrlStep(
            AgentAuthenticationResult authenticationResult) {
        return authenticationResult
                .getAuthenticationProcessStepIdentifier()
                .map(
                        stepIdentifier ->
                                RedirectPreparationRedirectUrlStep.class
                                        .getSimpleName()
                                        .equals(stepIdentifier.getValue()))
                .orElse(false);
    }
}
