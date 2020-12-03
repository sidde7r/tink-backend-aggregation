package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.steps;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.persistence.FortisAuthData;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.persistence.FortisDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.persistence.FortisLegacyAuthData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentStartAuthenticationProcessRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;

public class AuthInitStepTest extends AbstractStepTest {

    private AuthInitStep step;

    @Before
    public void setup() {
        step = new AuthInitStep(new FortisDataAccessorFactory(objectMapper));
    }

    @Test
    public void shouldContinueLegacyFlow() throws Exception {
        // given
        FortisAuthData fortisAuthData = new FortisAuthData();
        fortisAuthData.setClientNumber("1012310123");
        fortisAuthData.setUsername("67030416123412340");
        fortisAuthData.setLegacyAuthData(new FortisLegacyAuthData("a", "b", "c", "d"));

        AgentStartAuthenticationProcessRequest request =
                new AgentStartAuthenticationProcessRequest(
                        AgentAuthenticationPersistedData.of(
                                "FortisAuthData", objectMapper.writeValueAsString(fortisAuthData)));

        // when
        AgentAuthenticationResult result = step.execute(request);

        // then
        assertStepResult(result, LegacyAutoAuthStep.class);
    }

    @Test
    public void shouldContinueAutoFlow() throws Exception {
        // given
        FortisAuthData fortisAuthData = new FortisAuthData();
        fortisAuthData.setClientNumber("1012310123");
        fortisAuthData.setUsername("67030416123412340");
        fortisAuthData.setOcraKey("OCRA");
        fortisAuthData.setOathTokenId("TOKEN_ID");
        fortisAuthData.setDeviceId("DEVICE_ID");
        fortisAuthData.setCardFrameId("CARDFRAME_ID");

        AgentStartAuthenticationProcessRequest request =
                new AgentStartAuthenticationProcessRequest(
                        AgentAuthenticationPersistedData.of(
                                "FortisAuthData", objectMapper.writeValueAsString(fortisAuthData)));

        // when
        AgentAuthenticationResult result = step.execute(request);

        // then
        assertStepResult(result, AutoAuthStep.class);
    }

    @Test
    public void shouldContinueManualFlow() throws Exception {
        // given
        FortisAuthData fortisAuthData = new FortisAuthData();
        fortisAuthData.setClientNumber("1012310123");
        fortisAuthData.setUsername("67030416123412340");

        AgentStartAuthenticationProcessRequest request =
                new AgentStartAuthenticationProcessRequest(
                        AgentAuthenticationPersistedData.of(
                                "FortisAuthData", objectMapper.writeValueAsString(fortisAuthData)));

        // when
        AgentAuthenticationResult result = step.execute(request);

        // then
        assertStepResult(result, ClientCredentialsGetStep.class);
    }
}
