package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.validate_consent;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.await_user_confirmation.N26AwaitUserConfirmationStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.validate_consent.rpc.ValidateConsentCombinedResponse;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessState;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentUserInteractionAuthenticationProcessRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.ExternalApiCallResult;

@Slf4j
public class N26ValidateConsentStep extends N26ValidateConsentBaseStep
        implements AgentAuthenticationProcessStep<
                AgentUserInteractionAuthenticationProcessRequest> {

    public N26ValidateConsentStep(
            N26ValidateConsentApiCall n26ValidateConsentApiCall, ObjectMapper objectMapper) {
        super(n26ValidateConsentApiCall, objectMapper);
    }

    @Override
    public AgentAuthenticationResult execute(
            AgentUserInteractionAuthenticationProcessRequest authenticationProcessRequest) {

        ExternalApiCallResult<ValidateConsentCombinedResponse> callResult =
                buildAndExecuteRequest(
                        authenticationProcessRequest.getAuthenticationPersistedData());
        return parseResponseToResult(
                callResult,
                authenticationProcessRequest.getAuthenticationPersistedData(),
                authenticationProcessRequest.getAuthenticationProcessState());
    }

    @Override
    protected AgentAuthenticationResult parseResponseToResult(
            ExternalApiCallResult<ValidateConsentCombinedResponse> callResult,
            AgentAuthenticationPersistedData persistedData,
            AgentAuthenticationProcessState processState) {

        Optional<AgentAuthenticationResult> resultOptional =
                parseCommonResponseToResult(callResult, persistedData);

        return resultOptional.orElseGet(
                () ->
                        new AgentProceedNextStepAuthenticationResult(
                                AgentAuthenticationProcessStepIdentifier.of(
                                        N26AwaitUserConfirmationStep.class.getSimpleName()),
                                processState,
                                persistedData));
    }
}
