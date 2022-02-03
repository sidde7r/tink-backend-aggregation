package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication;

import java.util.Optional;
import java.util.UUID;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentSucceededAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectPreparationRedirectUrlStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.RefreshTokenFailureError;
import se.tink.backend.aggregation.agentsplatform.framework.error.Error;

public class KbcRedirectAuthenticationRefreshTokenStepHandler {

    public AgentAuthenticationResult defineResultOfAccessTokenRefresh(
            AgentProceedNextStepAuthenticationRequest authenticationProcessRequest,
            AgentAuthenticationResult authenticationResult,
            boolean userAvailableForInteraction) {
        if (authenticationResult instanceof AgentSucceededAuthenticationResult) {

            return new AgentProceedNextStepAuthenticationResult(
                    AgentAuthenticationProcessStep.identifier(KbcConsentValidationStep.class),
                    authenticationProcessRequest.getAuthenticationPersistedData());
        } else if (shouldNotAllowNextStep(authenticationResult, userAvailableForInteraction)) {

            return new AgentFailedAuthenticationResult(
                    new RefreshTokenFailureError(
                            Error.builder()
                                    .uniqueId(UUID.randomUUID().toString())
                                    .errorMessage(
                                            "Access token refresh has failed. User must authenticate manually.")
                                    .errorCode("ACCESS_TOKEN_REFRESH_FAILED")
                                    .build()),
                    authenticationProcessRequest.getAuthenticationPersistedData());
        }

        return authenticationResult;
    }

    private boolean shouldNotAllowNextStep(
            AgentAuthenticationResult authenticationResult, boolean userAvailableForInteraction) {
        Optional<AgentAuthenticationProcessStepIdentifier> stepIdentifier =
                authenticationResult.getAuthenticationProcessStepIdentifier();

        return authenticationResult instanceof AgentProceedNextStepAuthenticationResult
                && !userAvailableForInteraction
                && stepIdentifier.isPresent()
                && stepIdentifier
                        .get()
                        .getValue()
                        .equals(RedirectPreparationRedirectUrlStep.class.getSimpleName());
    }
}
