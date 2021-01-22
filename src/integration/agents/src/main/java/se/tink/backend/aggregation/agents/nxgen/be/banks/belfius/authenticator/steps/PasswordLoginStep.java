package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.steps;

import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.AgentPlatformBelfiusApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.BelfiusProcessState;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.persistence.BelfiusDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.responsevalidator.AgentPlatformResponseValidator;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentSucceededAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AgentBankApiError;

@RequiredArgsConstructor
public class PasswordLoginStep
        implements AgentAuthenticationProcessStep<AgentProceedNextStepAuthenticationRequest> {

    @NonNull private final AgentPlatformBelfiusApiClient apiClient;
    @NonNull private final BelfiusSessionStorage sessionStorage;
    @NonNull private final BelfiusDataAccessorFactory dataAccessorFactory;
    @NonNull private final AgentPlatformResponseValidator agentPlatformResponseValidator;

    @Override
    public AgentAuthenticationResult execute(AgentProceedNextStepAuthenticationRequest request) {
        BelfiusProcessState processState =
                dataAccessorFactory
                        .createBelfiusProcessStateAccessor(request.getAuthenticationProcessState())
                        .getBelfiusProcessState();

        LoginResponse loginResponse = loginPw(processState);
        Optional<AgentBankApiError> error = agentPlatformResponseValidator.validate(loginResponse);

        if (error.isPresent()) {
            return new AgentFailedAuthenticationResult(
                    error.get(), request.getAuthenticationPersistedData());
        }
        passProcessStateToSessionStorage(processState);
        return new AgentSucceededAuthenticationResult(request.getAuthenticationPersistedData());
    }

    private void passProcessStateToSessionStorage(BelfiusProcessState processState) {
        sessionStorage.putSessionData(processState.getSessionId(), processState.getMachineId());
        sessionStorage.setRequestCounterAggregated(
                processState.incrementAndGetRequestCounterAggregated());
    }

    private LoginResponse loginPw(BelfiusProcessState processState) {
        return apiClient.loginPw(
                processState.getSessionId(),
                processState.getMachineId(),
                processState.incrementAndGetRequestCounterAggregated(),
                processState.getDeviceTokenHashed(),
                processState.getDeviceTokenHashedIosComparison(),
                processState.getEncryptedPassword());
    }
}
