package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.steps;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.AgentPlatformBelfiusApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.BelfiusProcessState;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.HumanInteractionDelaySimulator;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentUserInteractionAuthenticationProcessRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.fielddefinition.CardReaderLoginInputAgentField;

@RequiredArgsConstructor
public class RegisterDeviceLoginStep
        implements AgentAuthenticationProcessStep<
                AgentUserInteractionAuthenticationProcessRequest> {

    @NonNull private final AgentPlatformBelfiusApiClient apiClient;

    @Override
    public AgentAuthenticationResult execute(
            AgentUserInteractionAuthenticationProcessRequest request) {

        BelfiusProcessState processState =
                request.getAuthenticationProcessState().get(BelfiusProcessState.KEY);

        String code =
                request.getUserInteractionData()
                        .getFieldValue(CardReaderLoginInputAgentField.id())
                        .replace(" ", "");
        keepAlive(processState);
        new HumanInteractionDelaySimulator().delayExecution(500);
        authenticateWithCode(processState, code);

        return new AgentProceedNextStepAuthenticationResult(
                AgentAuthenticationProcessStepIdentifier.of(
                        RegisterDeviceGetSignCodeStep.class.getSimpleName()),
                request.getAuthenticationProcessState(),
                request.getAuthenticationPersistedData());
    }

    private void authenticateWithCode(BelfiusProcessState processState, String code) {
        apiClient.authenticateWithCode(
                processState.getSessionId(),
                processState.getMachineId(),
                processState.incrementAndGetRequestCounterAggregated(),
                code);
    }

    private void keepAlive(BelfiusProcessState processState) {
        apiClient.keepAlive(
                processState.getSessionId(),
                processState.getMachineId(),
                processState.incrementAndGetRequestCounterAggregated());
    }
}
