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
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.responsevalidator.LoginResponseStatus;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.signature.BelfiusSignatureCreator;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.framework.error.AuthenticationError;

public class SoftLoginStepTest extends BaseStepTest {

    public static final String SIGNATURE = "signature";

    @Test
    public void shouldAuthenticateNoError() {
        // given
        AgentPlatformBelfiusApiClient apiClient = Mockito.mock(AgentPlatformBelfiusApiClient.class);
        BelfiusSignatureCreator signer = Mockito.mock(BelfiusSignatureCreator.class);
        SoftLoginStep step =
                new SoftLoginStep(apiClient, signer, createBelfiusPersistedDataAccessorFactory());

        AgentProceedNextStepAuthenticationRequest request =
                createAgentProceedNextStepAuthenticationRequest(
                        BelfiusProcessState.builder()
                                .sessionId(SESSION_ID)
                                .machineId(MACHINE_ID)
                                .challenge(CHALLENGE)
                                .build(),
                        new BelfiusAuthenticationData()
                                .panNumber(PAN_NUMBER)
                                .deviceToken(DEVICE_TOKEN));

        when(signer.hash(DEVICE_TOKEN)).thenReturn(DEVICE_TOKEN_HASHED);
        when(signer.hash(DEVICE_TOKEN_HASHED)).thenReturn(DEVICE_TOKEN_HASHED_IOS_COMPARISON);
        when(signer.createSignatureSoft(CHALLENGE, DEVICE_TOKEN, PAN_NUMBER)).thenReturn(SIGNATURE);

        when(apiClient.login(
                        SESSION_ID,
                        MACHINE_ID,
                        "2",
                        DEVICE_TOKEN_HASHED,
                        DEVICE_TOKEN_HASHED_IOS_COMPARISON,
                        SIGNATURE))
                .thenReturn(Mockito.mock(LoginResponse.class, a -> LoginResponseStatus.NO_ERRORS));

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
                                SoftLoginFinishStep.class.getSimpleName()));

        verify(apiClient).bacProductList(SESSION_ID, MACHINE_ID, "1");

        assertThat(nextStepResult.getAuthenticationProcessState()).isPresent();
        BelfiusProcessState processState =
                nextStepResult.getAuthenticationProcessState().get().get(BelfiusProcessState.KEY);

        assertThat(processState.getDeviceTokenHashed()).isEqualTo(DEVICE_TOKEN_HASHED);
        assertThat(processState.getDeviceTokenHashedIosComparison())
                .isEqualTo(DEVICE_TOKEN_HASHED_IOS_COMPARISON);
    }

    @Test
    public void shouldAuthenticationFailWithError() {
        // given
        AgentPlatformBelfiusApiClient apiClient = Mockito.mock(AgentPlatformBelfiusApiClient.class);
        BelfiusSignatureCreator signer = Mockito.mock(BelfiusSignatureCreator.class);
        SoftLoginStep step =
                new SoftLoginStep(apiClient, signer, createBelfiusPersistedDataAccessorFactory());

        AgentProceedNextStepAuthenticationRequest request =
                createAgentProceedNextStepAuthenticationRequest(
                        BelfiusProcessState.builder()
                                .sessionId(SESSION_ID)
                                .machineId(MACHINE_ID)
                                .challenge(CHALLENGE)
                                .build(),
                        new BelfiusAuthenticationData()
                                .panNumber(PAN_NUMBER)
                                .deviceToken(DEVICE_TOKEN));

        when(signer.hash(DEVICE_TOKEN)).thenReturn(DEVICE_TOKEN_HASHED);
        when(signer.hash(DEVICE_TOKEN_HASHED)).thenReturn(DEVICE_TOKEN_HASHED_IOS_COMPARISON);
        when(signer.createSignatureSoft(CHALLENGE, DEVICE_TOKEN, PAN_NUMBER)).thenReturn(SIGNATURE);

        when(apiClient.login(
                        SESSION_ID,
                        MACHINE_ID,
                        "2",
                        DEVICE_TOKEN_HASHED,
                        DEVICE_TOKEN_HASHED_IOS_COMPARISON,
                        SIGNATURE))
                .thenReturn(
                        Mockito.mock(
                                LoginResponse.class, a -> LoginResponseStatus.ACCOUNT_BLOCKED));

        when(signer.hash(any())).thenReturn(DEVICE_TOKEN_HASHED);
        when(signer.hash(DEVICE_TOKEN_HASHED)).thenReturn(DEVICE_TOKEN_HASHED_IOS_COMPARISON);

        // when
        AgentAuthenticationResult result = step.execute(request);

        // then
        assertThat(result).isInstanceOf(AgentFailedAuthenticationResult.class);
        AgentFailedAuthenticationResult failedResult = (AgentFailedAuthenticationResult) result;

        verify(apiClient).bacProductList(SESSION_ID, MACHINE_ID, "1");

        assertThat(failedResult.getError()).isInstanceOf(AuthenticationError.class);
    }
}
