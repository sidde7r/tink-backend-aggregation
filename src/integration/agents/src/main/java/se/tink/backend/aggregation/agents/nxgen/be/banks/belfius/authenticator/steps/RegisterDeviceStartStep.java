package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.steps;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.AgentPlatformBelfiusApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.BelfiusProcessState;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.BelfiusProcessStateAccessor;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.persistence.BelfiusAuthenticationData;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.persistence.BelfiusDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.persistence.BelfiusPersistedDataAccessor;
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
    @NonNull private final BelfiusDataAccessorFactory persistedDataAccessorFactory;

    @Override
    public AgentAuthenticationResult execute(AgentProceedNextStepAuthenticationRequest request) {
        BelfiusProcessStateAccessor processStateAccessor =
                persistedDataAccessorFactory.createBelfiusProcessStateAccessor(
                        request.getAuthenticationProcessState());
        BelfiusPersistedDataAccessor persistedDataAccessor =
                persistedDataAccessorFactory.createBelfiusPersistedDataAccessor(
                        request.getAuthenticationPersistedData());
        BelfiusAuthenticationData persistenceData =
                persistedDataAccessor.getBelfiusAuthenticationData();
        BelfiusProcessState processState = processStateAccessor.getBelfiusProcessState();

        initProcessState(persistenceData, processState);

        return new AgentProceedNextStepAuthenticationResult(
                AgentAuthenticationProcessStepIdentifier.of(
                        RegisterDeviceGetLoginCodeStep.class.getSimpleName()),
                processStateAccessor.storeBelfiusProcessState(processState),
                persistedDataAccessor.storeBelfiusAuthenticationData(persistenceData));
    }

    private void initProcessState(
            BelfiusAuthenticationData persistenceData, BelfiusProcessState processState) {
        processState.setDeviceToken(BelfiusIdGenerationUtils.generateDeviceToken());
        openSession(processState);
        startFlow(processState);
        bacProductList(processState);
        sendIsDeviceRegistered(persistenceData, processState);
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
