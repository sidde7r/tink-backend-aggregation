package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.steps;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.AgentPlatformFortisApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities.CardInfoEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities.InitiateLoginValueEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities.SignatureEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities.UcrChallengesEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.persistence.FortisAuthData;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.persistence.FortisDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.InitializeLoginResponse;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessState;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.fielddefinition.CardReaderLoginInputAgentField;

@RunWith(MockitoJUnitRunner.class)
public class IdentAuthStepTest extends AbstractStepTest {

    @Mock private AgentPlatformFortisApiClient fortisApiClient;

    private IdentAuthStep step;

    @Before
    public void setup() {
        step = new IdentAuthStep(fortisApiClient, new FortisDataAccessorFactory(objectMapper));
    }

    @Test
    public void shouldInitializeLogin() throws Exception {
        // given
        SignatureEntity signatureEntity = new SignatureEntity();
        signatureEntity.setChallenges(Collections.singletonList("12345678"));
        UcrChallengesEntity ucr = new UcrChallengesEntity();
        ucr.setSignature(signatureEntity);

        CardInfoEntity cardInfoEntity = new CardInfoEntity();
        cardInfoEntity.setCardFrameId("CARDFRAME_ID");

        InitiateLoginValueEntity value = new InitiateLoginValueEntity();
        value.setUcr(ucr);
        value.setCardInfo(cardInfoEntity);
        InitializeLoginResponse initializeLoginResponse = new InitializeLoginResponse();
        initializeLoginResponse.setLoginSessionId("TEST_LOGIN_SESSIONID");
        initializeLoginResponse.setValue(value);

        when(fortisApiClient.initializeLoginTransaction(any(), any()))
                .thenReturn(initializeLoginResponse);

        FortisAuthData fortisAuthData = new FortisAuthData();
        fortisAuthData.setUsername("67030416123412340");
        fortisAuthData.setClientNumber("1234012340");

        AgentProceedNextStepAuthenticationRequest request =
                new AgentProceedNextStepAuthenticationRequest(
                        AgentAuthenticationProcessStepIdentifier.of("doesnt-matter"),
                        AgentAuthenticationProcessState.of("FortisProcessState", "{}"),
                        AgentAuthenticationPersistedData.of(
                                "FortisAuthData", objectMapper.writeValueAsString(fortisAuthData)));

        // when
        AgentAuthenticationResult result = step.execute(request);

        // then
        assertStepResult(result, CheckLoginResultStep.class);

        assertProcessStateContainsValue(result, "cardFrameId", "CARDFRAME_ID");
        assertProcessStateContainsValue(result, "loginSessionId", "TEST_LOGIN_SESSIONID");

        assertFieldRequested(result, CardReaderLoginInputAgentField.id());
    }
}
