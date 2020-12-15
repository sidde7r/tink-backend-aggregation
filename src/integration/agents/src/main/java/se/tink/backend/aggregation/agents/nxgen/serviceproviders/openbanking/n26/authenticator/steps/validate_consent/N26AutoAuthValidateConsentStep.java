package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.validate_consent;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.fetch_authorization_url.N26FetchAuthorizationUrlStep;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentDetailsResponse;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessState;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;
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

        ExternalApiCallResult<ConsentDetailsResponse> callResult =
                buildAndExecuteRequest(request.getAuthenticationPersistedData());

        return parseResponseToResult(
                callResult,
                request.getAuthenticationPersistedData(),
                request.getAuthenticationProcessState());
    }

    @Override
    protected AgentAuthenticationResult parseResponseToResult(
            ExternalApiCallResult<ConsentDetailsResponse> callResult,
            AgentAuthenticationPersistedData persistedData,
            AgentAuthenticationProcessState processState) {

        Optional<AgentAuthenticationResult> resultOptional =
                parseCommonResponseToResult(callResult, persistedData);
        return resultOptional.orElseGet(
                () -> {
                    log.info("AutoAuth: consent is invalid. Moving to manual authentication");
                    return new AgentProceedNextStepAuthenticationResult(
                            AgentAuthenticationProcessStepIdentifier.of(
                                    N26FetchAuthorizationUrlStep.class.getSimpleName()),
                            persistedData);
                });
    }
}
