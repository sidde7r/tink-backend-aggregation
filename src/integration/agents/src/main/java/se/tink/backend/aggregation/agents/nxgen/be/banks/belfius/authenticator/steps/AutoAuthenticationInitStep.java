package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.steps;

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.AgentPlatformBelfiusApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.BelfiusProcessState;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.BelfiusProcessStateAccessor;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.BelfiusSessionService;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.persistence.BelfiusAuthenticationData;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.persistence.BelfiusDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.persistence.BelfiusPersistedDataAccessor;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.PrepareLoginResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.signature.BelfiusSignatureCreator;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AgentBankApiError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.DeviceRegistrationError;

@AllArgsConstructor
@Slf4j
public class AutoAuthenticationInitStep
        implements AgentAuthenticationProcessStep<AgentProceedNextStepAuthenticationRequest> {

    private final AgentPlatformBelfiusApiClient apiClient;
    private final BelfiusSignatureCreator signer;
    private final BelfiusDataAccessorFactory dataAccessorFactory;

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
        Optional<AgentBankApiError> maybeError =
                initProcessState(
                        processState, belfiusPersistedDataAccessor.getBelfiusAuthenticationData());
        if (maybeError.isPresent()) {
            return new AgentFailedAuthenticationResult(
                    maybeError.get(),
                    belfiusPersistedDataAccessor.clearBelfiusAuthenticationData());
        }
        return new AgentProceedNextStepAuthenticationResult(
                AgentAuthenticationProcessStepIdentifier.of(
                        PasswordLoginEncryptStep.class.getSimpleName()),
                processStateAccessor.storeBelfiusProcessState(processState),
                belfiusPersistedDataAccessor.storeBelfiusAuthenticationData(persistence));
    }

    private Optional<AgentBankApiError> initProcessState(
            BelfiusProcessState processState, BelfiusAuthenticationData persistence) {
        requestConfigIos();
        new BelfiusSessionService(apiClient, processState).openSession("XXX");
        PrepareLoginResponse prepareLoginResponse = prepareLogin(persistence, processState);
        if (prepareLoginResponse.isDeviceRegistrationError()) {
            return Optional.of(new DeviceRegistrationError());
        }
        processState.setContractNumber(prepareLoginResponse.getContractNumber());
        prepareDeviceToken(processState, persistence);
        return Optional.empty();
    }

    private PrepareLoginResponse prepareLogin(
            BelfiusAuthenticationData persistence, BelfiusProcessState processState) {
        return apiClient.prepareLogin(
                processState.getSessionId(),
                processState.getMachineId(),
                processState.incrementAndGetRequestCounterAggregated(),
                persistence.getPanNumber());
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
