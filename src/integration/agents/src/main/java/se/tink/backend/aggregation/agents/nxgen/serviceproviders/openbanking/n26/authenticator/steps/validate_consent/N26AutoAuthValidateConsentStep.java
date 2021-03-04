package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.validate_consent;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.validate_consent.rpc.ValidateConsentCombinedResponse;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessState;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.SessionExpiredError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.ExternalApiCallResult;

@Slf4j
public class N26AutoAuthValidateConsentStep extends N26ValidateConsentBaseStep
        implements AgentAuthenticationProcessStep<AgentProceedNextStepAuthenticationRequest> {

    public N26AutoAuthValidateConsentStep(
            N26ValidateConsentApiCall n26ValidateConsentApiCall, ObjectMapper objectMapper) {
        super(n26ValidateConsentApiCall, objectMapper);
    }

    @Override
    public AgentAuthenticationResult execute(AgentProceedNextStepAuthenticationRequest request) {

        ExternalApiCallResult<ValidateConsentCombinedResponse> callResult =
                buildAndExecuteRequest(request.getAuthenticationPersistedData());

        return parseResponseToResult(
                callResult,
                request.getAuthenticationPersistedData(),
                request.getAuthenticationProcessState());
    }

    @Override
    protected AgentAuthenticationResult parseResponseToResult(
            ExternalApiCallResult<ValidateConsentCombinedResponse> callResult,
            AgentAuthenticationPersistedData persistedData,
            AgentAuthenticationProcessState processState) {

        Optional<AgentAuthenticationResult> resultOptional =
                parseCommonResponseToResult(callResult, persistedData);
        return resultOptional.orElseGet(
                () -> new AgentFailedAuthenticationResult(new SessionExpiredError(), null));
    }
}
