package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.steps;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.AgentPlatformBelfiusApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.BelfiusProcessState;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.BelfiusProcessStateAccessor;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.persistence.BelfiusAuthenticationData;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.persistence.BelfiusDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.persistence.BelfiusPersistedDataAccessor;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.PrepareAuthenticationResponse;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentUserInteractionDefinitionResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.fielddefinition.CardReaderLoginDescriptionAgentField;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.fielddefinition.CardReaderLoginInputAgentField;
import se.tink.backend.aggregation.agentsplatform.framework.error.InvalidCredentialsError;

@RequiredArgsConstructor
@Slf4j
public class RegisterDeviceGetLoginCodeStep
        implements AgentAuthenticationProcessStep<AgentProceedNextStepAuthenticationRequest> {

    @NonNull private final AgentPlatformBelfiusApiClient apiClient;
    @NonNull private final BelfiusDataAccessorFactory belfiusDataAccessorFactory;

    @Override
    public AgentAuthenticationResult execute(AgentProceedNextStepAuthenticationRequest request) {
        BelfiusProcessStateAccessor processStateAccessor =
                belfiusDataAccessorFactory.createBelfiusProcessStateAccessor(
                        request.getAuthenticationProcessState());
        BelfiusPersistedDataAccessor persistedDataAccessor =
                belfiusDataAccessorFactory.createBelfiusPersistedDataAccessor(
                        request.getAuthenticationPersistedData());
        BelfiusAuthenticationData persistenceData =
                persistedDataAccessor.getBelfiusAuthenticationData();
        BelfiusProcessState processState = processStateAccessor.getBelfiusProcessState();
        PrepareAuthenticationResponse response =
                prepareAuthentication(persistenceData, processState);
        if (response.isCredentialsOk()) {
            return new AgentUserInteractionDefinitionResult(
                    AgentAuthenticationProcessStepIdentifier.of(
                            RegisterDeviceLoginStep.class.getSimpleName()),
                    persistedDataAccessor.storeBelfiusAuthenticationData(persistenceData),
                    processStateAccessor.storeBelfiusProcessState(processState),
                    new CardReaderLoginDescriptionAgentField(response.getChallenge()),
                    new CardReaderLoginInputAgentField());
        }
        return new AgentFailedAuthenticationResult(
                new InvalidCredentialsError(),
                persistedDataAccessor.clearBelfiusAuthenticationData());
    }

    private PrepareAuthenticationResponse prepareAuthentication(
            BelfiusAuthenticationData persistence, BelfiusProcessState processState) {
        return apiClient.prepareAuthentication(
                processState.getSessionId(),
                processState.getMachineId(),
                processState.incrementAndGetRequestCounterAggregated(),
                persistence.getPanNumber());
    }
}
