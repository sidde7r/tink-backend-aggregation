package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.steps;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.AgentPlatformBelfiusApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.BelfiusProcessState;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.persistence.BelfiusAuthenticationData;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.persistence.BelfiusPersistedData;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.persistence.BelfiusPersistedDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.responsevalidator.LoginResponseStatus;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.signature.BelfiusSignatureCreator;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;

@RequiredArgsConstructor
public class SoftLoginStep
        implements AgentAuthenticationProcessStep<AgentProceedNextStepAuthenticationRequest> {

    @NonNull private final AgentPlatformBelfiusApiClient apiClient;
    @NonNull private final BelfiusSignatureCreator signer;
    @NonNull private final BelfiusPersistedDataAccessorFactory persistedDataAccessorFactory;

    @Override
    public AgentAuthenticationResult execute(AgentProceedNextStepAuthenticationRequest request) {
        BelfiusPersistedData persistenceData =
                persistedDataAccessorFactory.createBelfiusPersistedDataAccessor(
                        request.getAuthenticationPersistedData());
        BelfiusAuthenticationData authenticatonData =
                persistenceData.getBelfiusAuthenticationData();
        BelfiusProcessState processState =
                request.getAuthenticationProcessState().get(BelfiusProcessState.KEY);

        String token = authenticatonData.getDeviceToken();
        String tokenHashed = signer.hash(token);
        processState.setDeviceTokenHashed(tokenHashed);
        String deviceTokenHashedIosComparison = signer.hash(tokenHashed);
        processState.setDeviceTokenHashedIosComparison(deviceTokenHashedIosComparison);
        String panNumber = authenticatonData.getPanNumber();
        String signature =
                signer.createSignatureSoft(processState.getChallenge(), token, panNumber);

        bacProductList(processState);
        LoginResponse loginResponse =
                login(processState, tokenHashed, deviceTokenHashedIosComparison, signature);
        LoginResponseStatus status = loginResponse.getStatus();
        if (status.isError()) {
            return new AgentFailedAuthenticationResult(
                    status.getError(), request.getAuthenticationPersistedData());
        }

        return new AgentProceedNextStepAuthenticationResult(
                AgentAuthenticationProcessStepIdentifier.of(
                        SoftLoginFinishStep.class.getSimpleName()),
                request.getAuthenticationProcessState(),
                persistenceData.storeBelfiusAuthenticationData(authenticatonData));
    }

    private LoginResponse login(
            BelfiusProcessState processState,
            String tokenHashed,
            String deviceTokenHashedIosComparison,
            String signature) {
        return apiClient.login(
                processState.getSessionId(),
                processState.getMachineId(),
                processState.incrementAndGetRequestCounterAggregated(),
                tokenHashed,
                deviceTokenHashedIosComparison,
                signature);
    }

    private void bacProductList(BelfiusProcessState processState) {
        apiClient.bacProductList(
                processState.getSessionId(),
                processState.getMachineId(),
                processState.incrementAndGetRequestCounterAggregated());
    }
}
