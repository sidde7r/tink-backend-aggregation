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
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.PrepareLoginResponse;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AgentBankApiError;

@RequiredArgsConstructor
public class SoftLoginGetContactNumberAndChallegeStep
        implements AgentAuthenticationProcessStep<AgentProceedNextStepAuthenticationRequest> {

    @NonNull private final AgentPlatformBelfiusApiClient apiClient;
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

        PrepareLoginResponse response = prepareLogin(persistenceData, processState);

        Optional<AgentBankApiError> error = agentPlatformResponseValidator.validate(response);

        if (error.isPresent()) {
            return new AgentFailedAuthenticationResult(
                    error.get(),
                    persistedDataAccessor.storeBelfiusAuthenticationData(persistenceData));
        }

        processState.setContractNumber(response.getContractNumber());
        processState.setChallenge(response.getChallenge());

        return new AgentProceedNextStepAuthenticationResult(
                AgentAuthenticationProcessStepIdentifier.of(SoftLoginStep.class.getSimpleName()),
                processStateAccessor.storeBelfiusProcessState(processState),
                persistedDataAccessor.storeBelfiusAuthenticationData(persistenceData));
    }

    private PrepareLoginResponse prepareLogin(
            BelfiusAuthenticationData persistence, BelfiusProcessState processState) {
        return apiClient.prepareLogin(
                processState.getSessionId(),
                processState.getMachineId(),
                processState.incrementAndGetRequestCounterAggregated(),
                persistence.getPanNumber());
    }
}
