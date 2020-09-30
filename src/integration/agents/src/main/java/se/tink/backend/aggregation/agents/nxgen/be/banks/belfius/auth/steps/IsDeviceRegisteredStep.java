package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.steps;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.AgentPlatformBelfiusApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.BelfiusProcessState;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.persistence.BelfiusAuthenticationData;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.persistence.BelfiusPersistedData;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.persistence.BelfiusPersistedDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.SessionOpenedResponse;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;

@RequiredArgsConstructor
@Slf4j
public class IsDeviceRegisteredStep
        implements AgentAuthenticationProcessStep<AgentProceedNextStepAuthenticationRequest> {

    @NonNull private final AgentPlatformBelfiusApiClient apiClient;
    @NonNull private final BelfiusPersistedDataAccessorFactory persistedDataAccessorFactory;

    @Override
    public AgentAuthenticationResult execute(AgentProceedNextStepAuthenticationRequest request) {
        BelfiusPersistedData persistenceData =
                persistedDataAccessorFactory.createBelfiusPersistedDataAccessor(
                        request.getAuthenticationPersistedData());
        BelfiusAuthenticationData authenticationData =
                persistenceData.getBelfiusAuthenticationData();
        BelfiusProcessState processState =
                request.getAuthenticationProcessState().get(BelfiusProcessState.KEY);

        if (isDeviceRegistered(authenticationData, processState)) {
            return new AgentProceedNextStepAuthenticationResult(
                    AgentAuthenticationProcessStepIdentifier.of(
                            SoftLoginInitStep.class.getSimpleName()),
                    request.getAuthenticationProcessState(),
                    request.getAuthenticationPersistedData());
        }
        return new AgentProceedNextStepAuthenticationResult(
                AgentAuthenticationProcessStepIdentifier.of(
                        RegisterDeviceStartStep.class.getSimpleName()),
                request.getAuthenticationProcessState(),
                persistenceData.storeBelfiusAuthenticationData(authenticationData));
    }

    private boolean isDeviceRegistered(
            BelfiusAuthenticationData persistence, BelfiusProcessState processState) {
        String panNumber = persistence.getPanNumber();
        String token = persistence.getDeviceToken();
        if (null == token) {
            return false;
        }
        openSession(processState);

        startFlow(processState);

        return apiClient.isDeviceRegistered(
                processState.getSessionId(),
                processState.getMachineId(),
                processState.incrementAndGetRequestCounterAggregated(),
                panNumber,
                token);
    }

    private void startFlow(BelfiusProcessState processState) {
        apiClient.startFlow(
                processState.getSessionId(),
                processState.getMachineId(),
                processState.incrementAndGetRequestCounterAggregated());
    }

    private void openSession(BelfiusProcessState processState) {
        SessionOpenedResponse sessionOpenedResponse = apiClient.openSession("XXX");
        processState.setSessionId(sessionOpenedResponse.getSessionId());
        processState.setMachineId(sessionOpenedResponse.getMachineIdentifier());
    }
}
