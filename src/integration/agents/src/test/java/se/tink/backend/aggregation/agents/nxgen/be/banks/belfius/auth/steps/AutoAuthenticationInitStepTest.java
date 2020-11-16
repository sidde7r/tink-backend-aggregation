package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.AgentPlatformBelfiusApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.BelfiusProcessState;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.persistence.BelfiusAuthenticationData;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.PrepareLoginResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.SessionOpenedResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.signature.BelfiusSignatureCreator;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.utils.BelfiusStringUtils;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;

public class AutoAuthenticationInitStepTest extends BaseStep {

    @Test
    public void shouldStartFlowAndGenerateHashes() {
        // given
        AgentPlatformBelfiusApiClient apiClient = Mockito.mock(AgentPlatformBelfiusApiClient.class);
        BelfiusSignatureCreator signer = Mockito.mock(BelfiusSignatureCreator.class);
        AutoAuthenticationInitStep step =
                new AutoAuthenticationInitStep(
                        apiClient, signer, createBelfiusDataAccessorFactory());

        AgentProceedNextStepAuthenticationRequest request =
                createAgentProceedNextStepAuthenticationRequest(
                        new BelfiusProcessState(),
                        new BelfiusAuthenticationData()
                                .panNumber(PAN_NUMBER)
                                .deviceToken(DEVICE_TOKEN));

        when(apiClient.openSession("XXX"))
                .thenReturn(new SessionOpenedResponse(SESSION_ID, MACHINE_ID, 1));

        when(apiClient.prepareLogin(
                        SESSION_ID,
                        MACHINE_ID,
                        "2",
                        BelfiusStringUtils.formatPanNumber(PAN_NUMBER)))
                .thenReturn(Mockito.mock(PrepareLoginResponse.class, a -> CONTRACT_NUMBER));

        when(signer.hash(any())).thenReturn(DEVICE_TOKEN_HASHED);
        when(signer.hash(DEVICE_TOKEN_HASHED)).thenReturn(DEVICE_TOKEN_HASHED_IOS_COMPARISON);

        // when
        AgentAuthenticationResult result = step.execute(request);

        // then
        assertThat(result).isInstanceOf(AgentProceedNextStepAuthenticationResult.class);
        AgentProceedNextStepAuthenticationResult nextStepResult =
                (AgentProceedNextStepAuthenticationResult) result;
        assertThat(nextStepResult.getAuthenticationProcessStepIdentifier().get())
                .isEqualTo(
                        AgentAuthenticationProcessStepIdentifier.of(
                                PasswordLoginEncryptStep.class.getSimpleName()));

        verify(apiClient).requestConfigIos();

        verify(apiClient).startFlow(SESSION_ID, MACHINE_ID, "1");

        assertThat(nextStepResult.getAuthenticationProcessState()).isPresent();
        BelfiusProcessState processState =
                createBelfiusDataAccessorFactory()
                        .createBelfiusProcessStateAccessor(
                                nextStepResult.getAuthenticationProcessState().get())
                        .getBelfiusProcessState();

        assertThat(processState.getSessionId()).isEqualTo(SESSION_ID);
        assertThat(processState.getMachineId()).isEqualTo(MACHINE_ID);
        assertThat(processState.getContractNumber()).isEqualTo(CONTRACT_NUMBER);
        assertThat(processState.getDeviceTokenHashed()).isEqualTo(DEVICE_TOKEN_HASHED);
        assertThat(processState.getDeviceTokenHashedIosComparison())
                .isEqualTo(DEVICE_TOKEN_HASHED_IOS_COMPARISON);
    }
}
