package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.steps;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.AgentPlatformBelfiusApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.BelfiusProcessState;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.BelfiusProcessStateAccessor;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.persistence.BelfiusDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.HumanInteractionDelaySimulator;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.AuthenticateWithCodeResponse;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentUserInteractionAuthenticationProcessRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.fielddefinition.CardReaderLoginInputAgentField;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.IncorrectCardReaderResponseCodeError;

@RequiredArgsConstructor
public class RegisterDeviceLoginStep
        implements AgentAuthenticationProcessStep<
                AgentUserInteractionAuthenticationProcessRequest> {

    @NonNull private final AgentPlatformBelfiusApiClient apiClient;
    @NonNull private final BelfiusDataAccessorFactory belfiusDataAccessorFactory;

    @Override
    public AgentAuthenticationResult execute(
            AgentUserInteractionAuthenticationProcessRequest request) {
        BelfiusProcessStateAccessor processStateAccessor =
                belfiusDataAccessorFactory.createBelfiusProcessStateAccessor(
                        request.getAuthenticationProcessState());
        BelfiusProcessState processState = processStateAccessor.getBelfiusProcessState();

        String code =
                request.getUserInteractionData()
                        .getFieldValue(CardReaderLoginInputAgentField.id())
                        .replace(" ", "");
        keepAlive(processState);
        new HumanInteractionDelaySimulator().delayExecution(500);
        AuthenticateWithCodeResponse authenticateWithCodeResponse =
                authenticateWithCode(processState, code);
        if (authenticateWithCodeResponse.isChallengeResponseOk()) {
            return new AgentProceedNextStepAuthenticationResult(
                    AgentAuthenticationProcessStepIdentifier.of(
                            RegisterDeviceGetSignCodeStep.class.getSimpleName()),
                    processStateAccessor.storeBelfiusProcessState(processState),
                    request.getAuthenticationPersistedData());
        }
        return new AgentFailedAuthenticationResult(
                new IncorrectCardReaderResponseCodeError(),
                request.getAuthenticationPersistedData());
    }

    private AuthenticateWithCodeResponse authenticateWithCode(
            BelfiusProcessState processState, String code) {
        return apiClient.authenticateWithCode(
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
