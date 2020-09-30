package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.steps;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.AgentPlatformBelfiusApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.BelfiusProcessState;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.persistence.BelfiusAuthenticationData;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.persistence.BelfiusPersistedDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.PrepareLoginResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.SessionOpenedResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.signature.BelfiusSignatureCreator;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;

@AllArgsConstructor
@Slf4j
public class AutoAuthenticationInitStep
        implements AgentAuthenticationProcessStep<AgentProceedNextStepAuthenticationRequest> {

    private final AgentPlatformBelfiusApiClient apiClient;
    private final BelfiusSignatureCreator signer;
    private final BelfiusPersistedDataAccessorFactory persistedDataAccessorFactory;

    @Override
    public AgentAuthenticationResult execute(AgentProceedNextStepAuthenticationRequest request) {
        BelfiusAuthenticationData persistence =
                persistedDataAccessorFactory
                        .createBelfiusPersistedDataAccessor(
                                request.getAuthenticationPersistedData())
                        .getBelfiusAuthenticationData();
        BelfiusProcessState processState =
                request.getAuthenticationProcessState().get(BelfiusProcessState.KEY);

        requestConfigIos();

        String deviceToken = persistence.getDeviceToken();

        openSession(processState);

        startFlow(processState);

        PrepareLoginResponse response = prepareLogin(persistence, processState);
        processState.setContractNumber(response.getContractNumber());
        String deviceTokenHashed = signer.hash(deviceToken);
        processState.setDeviceTokenHashed(deviceTokenHashed);
        String deviceTokenHashedIosComparison = signer.hash(deviceTokenHashed);
        processState.setDeviceTokenHashedIosComparison(deviceTokenHashedIosComparison);

        return new AgentProceedNextStepAuthenticationResult(
                AgentAuthenticationProcessStepIdentifier.of(
                        PasswordLoginEncryptStep.class.getSimpleName()),
                request.getAuthenticationProcessState(),
                request.getAuthenticationPersistedData());
    }

    private PrepareLoginResponse prepareLogin(
            BelfiusAuthenticationData persistence, BelfiusProcessState processState) {
        return apiClient.prepareLogin(
                processState.getSessionId(),
                processState.getMachineId(),
                processState.incrementAndGetRequestCounterAggregated(),
                persistence.getPanNumber());
    }

    private void openSession(BelfiusProcessState processState) {
        SessionOpenedResponse sessionOpenedResponse = apiClient.openSession("XXX");
        processState.setSessionId(sessionOpenedResponse.getSessionId());
        processState.setMachineId(sessionOpenedResponse.getMachineIdentifier());
    }

    private void startFlow(BelfiusProcessState processState) {
        apiClient.startFlow(
                processState.getSessionId(),
                processState.getMachineId(),
                processState.incrementAndGetRequestCounterAggregated());
    }

    private void requestConfigIos() {
        apiClient.requestConfigIos();
    }
}
