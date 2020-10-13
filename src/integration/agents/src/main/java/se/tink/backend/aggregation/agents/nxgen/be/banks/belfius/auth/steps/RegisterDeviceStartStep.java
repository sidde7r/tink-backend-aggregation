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
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.signature.BelfiusSignatureCreator;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.utils.BelfiusIdGenerationUtils;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;

@RequiredArgsConstructor
@Slf4j
public class RegisterDeviceStartStep
        implements AgentAuthenticationProcessStep<AgentProceedNextStepAuthenticationRequest> {

    @NonNull private final AgentPlatformBelfiusApiClient apiClient;
    @NonNull private final BelfiusSignatureCreator signer;
    @NonNull private final BelfiusPersistedDataAccessorFactory persistedDataAccessorFactory;

    @Override
    public AgentAuthenticationResult execute(AgentProceedNextStepAuthenticationRequest request) {
        BelfiusPersistedData persistenceData =
                persistedDataAccessorFactory.createBelfiusPersistedDataAccessor(
                        request.getAuthenticationPersistedData());
        BelfiusAuthenticationData authenticationData =
                persistenceData.getBelfiusAuthenticationData();

        initProcessState(request, authenticationData);

        return new AgentProceedNextStepAuthenticationResult(
                AgentAuthenticationProcessStepIdentifier.of(
                        RegisterDeviceGetLoginCodeStep.class.getSimpleName()),
                request.getAuthenticationProcessState(),
                persistenceData.storeBelfiusAuthenticationData(authenticationData));
    }

    private void initProcessState(
            AgentProceedNextStepAuthenticationRequest request,
            BelfiusAuthenticationData authenticationData) {
        BelfiusProcessState processState =
                request.getAuthenticationProcessState().get(BelfiusProcessState.KEY);
        processState.setDeviceToken(BelfiusIdGenerationUtils.generateDeviceToken());
        openSession(processState);
        startFlow(processState);
        bacProductList(processState);
        sendIsDeviceRegistered(authenticationData, processState);
    }

    private void sendIsDeviceRegistered(
            BelfiusAuthenticationData persistence, BelfiusProcessState processState) {
        apiClient.sendIsDeviceRegistered(
                processState.getSessionId(),
                processState.getMachineId(),
                processState.incrementAndGetRequestCounterServices(),
                persistence.getPanNumber(),
                signer.hash(processState.getDeviceToken()));
    }

    private void bacProductList(BelfiusProcessState processState) {
        apiClient.bacProductList(
                processState.getSessionId(),
                processState.getMachineId(),
                processState.incrementAndGetRequestCounterServices());
    }

    private void openSession(BelfiusProcessState processState) {
        SessionOpenedResponse sessionOpenedResponse = apiClient.openSession("XXX");
        processState.resetRequestCounterAggregated();
        processState.setSessionId(sessionOpenedResponse.getSessionId());
        processState.setMachineId(sessionOpenedResponse.getMachineIdentifier());
    }

    private void startFlow(BelfiusProcessState processState) {
        apiClient.startFlow(
                processState.getSessionId(),
                processState.getMachineId(),
                processState.incrementAndGetRequestCounterAggregated());
    }
}
