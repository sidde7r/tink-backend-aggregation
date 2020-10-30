package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.steps;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.AgentPlatformBelfiusApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.BelfiusProcessState;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.BelfiusProcessStateAccessor;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.BelfiusSessionService;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.persistence.BelfiusAuthenticationData;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.persistence.BelfiusDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.persistence.BelfiusPersistedDataAccessor;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessState;
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
    @NonNull private final BelfiusDataAccessorFactory dataAccessorFactory;

    @Override
    public AgentAuthenticationResult execute(AgentProceedNextStepAuthenticationRequest request) {
        BelfiusProcessStateAccessor processStateAccessor =
                dataAccessorFactory.createBelfiusProcessStateAccessor(
                        request.getAuthenticationProcessState());
        BelfiusPersistedDataAccessor belfiusPersistedDataAccessor =
                dataAccessorFactory.createBelfiusPersistedDataAccessor(
                        request.getAuthenticationPersistedData());
        BelfiusAuthenticationData persistence =
                belfiusPersistedDataAccessor.getBelfiusAuthenticationData();
        BelfiusProcessState processState = processStateAccessor.getBelfiusProcessState();

        if (isDeviceRegistered(persistence, processState)) {
            return softLoginResult(
                    processStateAccessor.storeBelfiusProcessState(processState),
                    belfiusPersistedDataAccessor.storeBelfiusAuthenticationData(persistence));
        }
        return registerDeviceResult(
                processStateAccessor.storeBelfiusProcessState(processState),
                belfiusPersistedDataAccessor.storeBelfiusAuthenticationData(persistence));
    }

    private AgentProceedNextStepAuthenticationResult softLoginResult(
            AgentAuthenticationProcessState processState,
            AgentAuthenticationPersistedData persistedData) {
        return new AgentProceedNextStepAuthenticationResult(
                AgentAuthenticationProcessStepIdentifier.of(
                        SoftLoginInitStep.class.getSimpleName()),
                processState,
                persistedData);
    }

    private AgentProceedNextStepAuthenticationResult registerDeviceResult(
            AgentAuthenticationProcessState processState,
            AgentAuthenticationPersistedData persistedData) {
        return new AgentProceedNextStepAuthenticationResult(
                AgentAuthenticationProcessStepIdentifier.of(
                        RegisterDeviceStartStep.class.getSimpleName()),
                processState,
                persistedData);
    }

    private boolean isDeviceRegistered(
            BelfiusAuthenticationData persistence, BelfiusProcessState processState) {
        String panNumber = persistence.getPanNumber();
        String token = persistence.getDeviceToken();
        if (null == token) {
            return false;
        }
        new BelfiusSessionService(apiClient, processState).openSession("XXX");

        return apiClient.isDeviceRegistered(
                processState.getSessionId(),
                processState.getMachineId(),
                processState.incrementAndGetRequestCounterAggregated(),
                panNumber,
                token);
    }
}
