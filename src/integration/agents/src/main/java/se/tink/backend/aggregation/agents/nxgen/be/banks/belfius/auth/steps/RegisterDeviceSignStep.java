package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.steps;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.AgentPlatformBelfiusApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.BelfiusProcessState;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.BelfiusProcessStateAccessor;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.persistence.BelfiusDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.RegisterDeviceSignResponse;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentUserInteractionAuthenticationProcessRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.fielddefinition.CardReaderSignInputAgentField;

@RequiredArgsConstructor
public class RegisterDeviceSignStep
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

        String sign =
                request.getUserInteractionData()
                        .getFieldValue(CardReaderSignInputAgentField.id())
                        .replace(" ", "");
        RegisterDeviceSignResponse registerDeviceResponse = registerDevice(processState, sign);

        if (registerDeviceResponse.checkForErrors().isPresent()) {
            return new AgentFailedAuthenticationResult(
                    registerDeviceResponse.checkForErrors().get(),
                    request.getAuthenticationPersistedData());
        }
        return new AgentProceedNextStepAuthenticationResult(
                AgentAuthenticationProcessStepIdentifier.of(
                        RegisterDeviceFinishStep.class.getSimpleName()),
                processStateAccessor.storeBelfiusProcessState(processState),
                request.getAuthenticationPersistedData());
    }

    private RegisterDeviceSignResponse registerDevice(
            BelfiusProcessState processState, String sign) {
        return apiClient.registerDevice(
                processState.getSessionId(),
                processState.getMachineId(),
                processState.incrementAndGetRequestCounterAggregated(),
                sign);
    }
}
