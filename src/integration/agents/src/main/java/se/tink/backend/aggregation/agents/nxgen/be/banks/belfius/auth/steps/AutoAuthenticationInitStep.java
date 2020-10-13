package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.steps;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.AgentPlatformBelfiusApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.BelfiusProcessState;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.BelfiusSessionService;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.persistence.BelfiusAuthenticationData;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.persistence.BelfiusPersistedDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.PrepareLoginResponse;
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
        initProcessState(request);
        return new AgentProceedNextStepAuthenticationResult(
                AgentAuthenticationProcessStepIdentifier.of(
                        PasswordLoginEncryptStep.class.getSimpleName()),
                request.getAuthenticationProcessState(),
                request.getAuthenticationPersistedData());
    }

    private void initProcessState(AgentProceedNextStepAuthenticationRequest request) {
        BelfiusAuthenticationData persistence =
                persistedDataAccessorFactory
                        .createBelfiusPersistedDataAccessor(
                                request.getAuthenticationPersistedData())
                        .getBelfiusAuthenticationData();
        BelfiusProcessState processState =
                request.getAuthenticationProcessState().get(BelfiusProcessState.KEY);
        requestConfigIos();
        new BelfiusSessionService(apiClient, processState).openSession("XXX");
        prepareLogin(persistence, processState);
        prepareDeviceToken(processState, persistence);
    }

    private void prepareLogin(
            BelfiusAuthenticationData persistence, BelfiusProcessState processState) {
        PrepareLoginResponse response =
                apiClient.prepareLogin(
                        processState.getSessionId(),
                        processState.getMachineId(),
                        processState.incrementAndGetRequestCounterAggregated(),
                        persistence.getPanNumber());
        processState.setContractNumber(response.getContractNumber());
    }

    private void requestConfigIos() {
        apiClient.requestConfigIos();
    }

    private void prepareDeviceToken(
            BelfiusProcessState processState, BelfiusAuthenticationData persistence) {
        String deviceTokenHashed = signer.hash(persistence.getDeviceToken());
        processState.setDeviceTokenHashed(deviceTokenHashed);
        processState.setDeviceTokenHashedIosComparison(signer.hash(deviceTokenHashed));
    }
}
