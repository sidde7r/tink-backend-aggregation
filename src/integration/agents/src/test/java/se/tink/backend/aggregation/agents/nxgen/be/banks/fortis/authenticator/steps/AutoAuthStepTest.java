package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.AgentPlatformFortisApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities.CardInfoEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities.EasyPinChallengeEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities.InitiateLoginValueEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.persistence.FortisAuthData;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.persistence.FortisDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.CheckLoginResultResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.InitializeLoginResponse;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessState;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentSucceededAuthenticationResult;

@RunWith(MockitoJUnitRunner.class)
public class AutoAuthStepTest extends AbstractStepTest {

    @Mock private AgentPlatformFortisApiClient fortisApiClient;

    private AutoAuthStep step;

    @Before
    public void setup() {
        step = new AutoAuthStep(fortisApiClient, new FortisDataAccessorFactory(objectMapper));
    }

    @Test
    public void shouldAuthenticate() throws Exception {
        // given
        EasyPinChallengeEntity ucr = new EasyPinChallengeEntity();
        ucr.setChallenge("12345678");

        CardInfoEntity cardInfoEntity = new CardInfoEntity();
        cardInfoEntity.setCardFrameId("CARDFRAME_ID");

        InitiateLoginValueEntity value = new InitiateLoginValueEntity();
        value.setEasyPin(ucr);
        value.setCardInfo(cardInfoEntity);
        InitializeLoginResponse initializeLoginResponse = new InitializeLoginResponse();
        initializeLoginResponse.setLoginSessionId("TEST_LOGIN_SESSIONID");
        initializeLoginResponse.setValue(value);

        when(fortisApiClient.initializeLoginTransaction(any(), any(), any(), any(), any()))
                .thenReturn(initializeLoginResponse);

        when(fortisApiClient.checkLoginResultEasyPin(any(), any()))
                .thenReturn(new CheckLoginResultResponse());

        FortisAuthData fortisAuthData = new FortisAuthData();
        fortisAuthData.setClientNumber("1012310123");
        fortisAuthData.setUsername("67030416123412340");
        fortisAuthData.setOcraKey("ABCD");
        fortisAuthData.setOathTokenId("TOKEN_ID");
        fortisAuthData.setDeviceId("DEVICE_ID");
        fortisAuthData.setCardFrameId("CARDFRAME_ID");

        AgentProceedNextStepAuthenticationRequest request =
                new AgentProceedNextStepAuthenticationRequest(
                        AgentAuthenticationProcessStepIdentifier.of("doesnt-matter"),
                        AgentAuthenticationProcessState.of("FortisProcessState", "{}"),
                        AgentAuthenticationPersistedData.of(
                                "FortisAuthData", objectMapper.writeValueAsString(fortisAuthData)));

        // when
        AgentAuthenticationResult result = step.execute(request);

        // then
        assertThat(result).isInstanceOf(AgentSucceededAuthenticationResult.class);
        verify(fortisApiClient)
                .initializeLoginTransaction(
                        eq("670304XXXXXXX2340"),
                        eq("CARDFRAME_ID"),
                        eq("1012310123"),
                        eq("TOKEN_ID"),
                        eq("DEVICE_ID"));
        verify(fortisApiClient).checkLoginResultEasyPin(eq("1012310123"), any());
        verify(fortisApiClient).doEbewAppLogin(eq("1012310123"), eq("30"));
    }
}
