package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.steps;

import com.github.javafaker.Faker;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.AgentPlatformBelfiusApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.BelfiusProcessState;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.BelfiusProcessStateAccessor;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.persistence.BelfiusDataAccessorFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentUserInteractionDefinitionResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.fielddefinition.CardReaderSignDescriptionAgentField;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.fielddefinition.CardReaderSignInputAgentField;

@RequiredArgsConstructor
@Slf4j
public class RegisterDeviceGetSignCodeStep
        implements AgentAuthenticationProcessStep<AgentProceedNextStepAuthenticationRequest> {

    @NonNull private final AgentPlatformBelfiusApiClient apiClient;
    @NonNull private final BelfiusDataAccessorFactory belfiusDataAccessorFactory;

    @Override
    public AgentAuthenticationResult execute(AgentProceedNextStepAuthenticationRequest request) {
        BelfiusProcessStateAccessor processStateAccessor =
                belfiusDataAccessorFactory.createBelfiusProcessStateAccessor(
                        request.getAuthenticationProcessState());
        BelfiusProcessState processState = processStateAccessor.getBelfiusProcessState();

        consultClientSettings(processState);

        String challenge = prepareDeviceRegistration(processState);

        return new AgentUserInteractionDefinitionResult(
                AgentAuthenticationProcessStepIdentifier.of(
                        RegisterDeviceSignStep.class.getSimpleName()),
                request.getAuthenticationPersistedData(),
                processStateAccessor.storeBelfiusProcessState(processState),
                new CardReaderSignDescriptionAgentField(challenge),
                new CardReaderSignInputAgentField());
    }

    private void consultClientSettings(BelfiusProcessState processState) {
        apiClient.consultClientSettings(
                processState.getSessionId(),
                processState.getMachineId(),
                processState.incrementAndGetRequestCounterServices());
    }

    private String prepareDeviceRegistration(BelfiusProcessState processState) {
        String name = new Faker().name().firstName();
        log.info("Belfius - Generated model name: {}", name);
        return apiClient.prepareDeviceRegistration(
                processState.getSessionId(),
                processState.getMachineId(),
                processState.incrementAndGetRequestCounterAggregated(),
                processState.getDeviceToken(),
                BelfiusConstants.BRAND,
                name);
    }
}
