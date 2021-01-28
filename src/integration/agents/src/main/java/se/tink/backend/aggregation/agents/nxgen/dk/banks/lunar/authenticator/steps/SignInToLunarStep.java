package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.steps;

import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.AgentPlatformLunarApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.exception.LunarAuthenticationExceptionHandler;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarDataAccessorFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AccessTokenFetchingFailureError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AgentBankApiError;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@Slf4j
public class SignInToLunarStep extends AbstractSignInToLunarStep {

    public SignInToLunarStep(
            LunarDataAccessorFactory dataAccessorFactory, AgentPlatformLunarApiClient apiClient) {
        super(dataAccessorFactory, apiClient);
    }

    @Override
    AgentFailedAuthenticationResult getFailedAuthResult(
            AgentProceedNextStepAuthenticationRequest request, HttpResponseException e) {
        log.error("Failed to signIn to Lunar", e);
        return new AgentFailedAuthenticationResult(
                LunarAuthenticationExceptionHandler.toKnownErrorFromResponseOrDefault(
                        e, getDefaultError()),
                request.getAuthenticationPersistedData());
    }

    @Override
    AgentBankApiError getDefaultError() {
        return new AccessTokenFetchingFailureError();
    }
}
