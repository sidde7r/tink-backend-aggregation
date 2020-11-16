package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.AgentPlatformBelfiusApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.BelfiusProcessState;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.persistence.BelfiusAuthenticationData;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.SendCardNumberResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.signature.BelfiusSignatureCreator;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;

public class PasswordLoginEncryptStepTest extends BaseStep {

    @Test
    public void shouldEncryptPassword() {
        // given
        AgentPlatformBelfiusApiClient apiClient = Mockito.mock(AgentPlatformBelfiusApiClient.class);
        BelfiusSignatureCreator signer = Mockito.mock(BelfiusSignatureCreator.class);
        PasswordLoginEncryptStep step =
                new PasswordLoginEncryptStep(apiClient, signer, createBelfiusDataAccessorFactory());
        BelfiusAuthenticationData persistenceData =
                new BelfiusAuthenticationData()
                        .deviceToken(DEVICE_TOKEN)
                        .panNumber(PAN_NUMBER)
                        .password(PASSWORD);
        AgentProceedNextStepAuthenticationRequest request =
                createAgentProceedNextStepAuthenticationRequest(
                        new BelfiusProcessState()
                                .contractNumber(CONTRACT_NUMBER)
                                .sessionId(SESSION_ID)
                                .machineId(MACHINE_ID),
                        persistenceData);

        when(apiClient.sendCardNumber(SESSION_ID, MACHINE_ID, "1", PAN_NUMBER))
                .thenReturn(Mockito.mock(SendCardNumberResponse.class, a -> CHALLENGE));

        when(signer.createSignaturePw(CHALLENGE, CONTRACT_NUMBER, persistenceData))
                .thenReturn(ENCRYPTED_PASSWORD);

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
                                PasswordLoginStep.class.getSimpleName()));

        assertThat(nextStepResult.getAuthenticationProcessState()).isPresent();
        BelfiusProcessState processState =
                createBelfiusDataAccessorFactory()
                        .createBelfiusProcessStateAccessor(
                                nextStepResult.getAuthenticationProcessState().get())
                        .getBelfiusProcessState();

        assertThat(processState.getEncryptedPassword()).isEqualTo(ENCRYPTED_PASSWORD);
    }
}
