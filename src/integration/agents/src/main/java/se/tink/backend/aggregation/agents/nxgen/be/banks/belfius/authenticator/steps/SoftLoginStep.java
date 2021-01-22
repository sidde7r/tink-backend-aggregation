package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.steps;

import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.AgentPlatformBelfiusApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.BelfiusProcessState;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.BelfiusProcessStateAccessor;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.persistence.BelfiusAuthenticationData;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.persistence.BelfiusDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.persistence.BelfiusPersistedDataAccessor;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.responsevalidator.AgentPlatformResponseValidator;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.signature.BelfiusSignatureCreator;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AgentBankApiError;

@RequiredArgsConstructor
public class SoftLoginStep
        implements AgentAuthenticationProcessStep<AgentProceedNextStepAuthenticationRequest> {

    @NonNull private final AgentPlatformBelfiusApiClient apiClient;
    @NonNull private final BelfiusSignatureCreator signer;
    @NonNull private final BelfiusDataAccessorFactory persistedDataAccessorFactory;
    @NonNull private final AgentPlatformResponseValidator agentPlatformResponseValidator;

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

        initDeviceToken(processState, persistenceData);
        String signature =
                signer.createSignatureSoft(
                        processState.getChallenge(),
                        persistenceData.getDeviceToken(),
                        persistenceData.getPanNumber());
        bacProductList(processState);
        LoginResponse loginResponse =
                login(
                        processState,
                        processState.getDeviceTokenHashed(),
                        processState.getDeviceTokenHashedIosComparison(),
                        signature);
        Optional<AgentBankApiError> error = agentPlatformResponseValidator.validate(loginResponse);
        if (error.isPresent()) {
            return new AgentFailedAuthenticationResult(
                    error.get(),
                    persistedDataAccessor.storeBelfiusAuthenticationData(persistenceData));
        }

        return new AgentProceedNextStepAuthenticationResult(
                AgentAuthenticationProcessStepIdentifier.of(
                        SoftLoginFinishStep.class.getSimpleName()),
                processStateAccessor.storeBelfiusProcessState(processState),
                persistedDataAccessor.storeBelfiusAuthenticationData(persistenceData));
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

    private void initDeviceToken(
            BelfiusProcessState processState, BelfiusAuthenticationData persistenceData) {
        String token = persistenceData.getDeviceToken();
        String tokenHashed = signer.hash(token);
        processState.setDeviceTokenHashed(tokenHashed);
        String deviceTokenHashedIosComparison = signer.hash(tokenHashed);
        processState.setDeviceTokenHashedIosComparison(deviceTokenHashedIosComparison);
    }
}
