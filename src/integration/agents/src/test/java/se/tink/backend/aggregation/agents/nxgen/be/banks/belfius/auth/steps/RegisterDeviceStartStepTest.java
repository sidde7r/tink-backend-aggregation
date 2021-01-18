package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.AgentPlatformBelfiusApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.BelfiusProcessState;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.persistence.BelfiusAuthenticationData;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.steps.RegisterDeviceGetLoginCodeStep;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.steps.RegisterDeviceStartStep;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.SessionOpenedResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.signature.BelfiusSignatureCreator;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.utils.BelfiusStringUtils;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.common.AgentExtendedClientInfo;

public class RegisterDeviceStartStepTest extends BaseStep {

    @Test
    public void shouldStartFlow() {
        // given
        AgentPlatformBelfiusApiClient apiClient = Mockito.mock(AgentPlatformBelfiusApiClient.class);
        BelfiusSignatureCreator signer = Mockito.mock(BelfiusSignatureCreator.class);
        RegisterDeviceStartStep step =
                new RegisterDeviceStartStep(apiClient, signer, createBelfiusDataAccessorFactory());

        AgentProceedNextStepAuthenticationRequest request =
                createAgentProceedNextStepAuthenticationRequest(
                        new BelfiusProcessState(),
                        new BelfiusAuthenticationData().panNumber(PAN_NUMBER),
                        AgentExtendedClientInfo.builder().build());

        when(apiClient.openSession("XXX"))
                .thenReturn(new SessionOpenedResponse(SESSION_ID, MACHINE_ID, 1));

        when(signer.hash(any())).thenReturn(DEVICE_TOKEN_HASHED);

        // when
        AgentAuthenticationResult result = step.execute(request);

        // then
        assertThat(result).isInstanceOf(AgentProceedNextStepAuthenticationResult.class);
        AgentProceedNextStepAuthenticationResult nextStepResult =
                (AgentProceedNextStepAuthenticationResult) result;
        assertThat(nextStepResult.getAuthenticationProcessStepIdentifier().get())
                .isEqualTo(
                        AgentAuthenticationProcessStepIdentifier.of(
                                RegisterDeviceGetLoginCodeStep.class.getSimpleName()));

        verify(apiClient).startFlow(SESSION_ID, MACHINE_ID, "1");

        verify(apiClient).bacProductList(SESSION_ID, MACHINE_ID, "1");

        verify(apiClient)
                .sendIsDeviceRegistered(
                        SESSION_ID,
                        MACHINE_ID,
                        "2",
                        BelfiusStringUtils.formatPanNumber(PAN_NUMBER),
                        DEVICE_TOKEN_HASHED);

        assertThat(nextStepResult.getAuthenticationProcessState()).isPresent();
        BelfiusProcessState processState =
                createBelfiusDataAccessorFactory()
                        .createBelfiusProcessStateAccessor(
                                nextStepResult.getAuthenticationProcessState().get())
                        .getBelfiusProcessState();

        assertThat(processState.getDeviceToken()).isNotEmpty();
    }
}
