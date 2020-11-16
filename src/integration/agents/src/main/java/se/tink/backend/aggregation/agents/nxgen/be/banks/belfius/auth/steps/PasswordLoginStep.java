package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.steps;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.AgentPlatformBelfiusApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.BelfiusProcessState;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.persistence.BelfiusDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.responsevalidator.LoginResponseStatus;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentSucceededAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;

@RequiredArgsConstructor
public class PasswordLoginStep
        implements AgentAuthenticationProcessStep<AgentProceedNextStepAuthenticationRequest> {

    @NonNull private final AgentPlatformBelfiusApiClient apiClient;
    @NonNull private final BelfiusSessionStorage sessionStorage;
    @NonNull private final BelfiusDataAccessorFactory dataAccessorFactory;

    @Override
    public AgentAuthenticationResult execute(AgentProceedNextStepAuthenticationRequest request) {
        BelfiusProcessState processState =
                dataAccessorFactory
                        .createBelfiusProcessStateAccessor(request.getAuthenticationProcessState())
                        .getBelfiusProcessState();

        LoginResponseStatus status = loginPw(processState).getStatus();

        if (status.isError()) {
            return new AgentFailedAuthenticationResult(
                    status.getError(), request.getAuthenticationPersistedData());
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
