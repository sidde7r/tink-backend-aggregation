package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.AgentPlatformFortisApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities.EasyPinActivateValueEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.persistence.FortisAuthData;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.persistence.FortisDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.persistence.FortisProcessState;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.EasyPinActivateResponse;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessState;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentSucceededAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.common.AgentExtendedClientInfo;

@RunWith(MockitoJUnitRunner.class)
public class EasyPinActivationStepTest extends AbstractStepTest {

    @Mock private AgentPlatformFortisApiClient fortisApiClient;

    private EasyPinActivationStep step;

    @Before
    public void setup() {
        step =
                new EasyPinActivationStep(
                        fortisApiClient, new FortisDataAccessorFactory(objectMapper));
    }

    @Test
    public void shouldActivateAndSucceed() throws Exception {
        // given
        EasyPinActivateValueEntity value = new EasyPinActivateValueEntity();
        value.setTokenValid(true);

        EasyPinActivateResponse response = new EasyPinActivateResponse();
        response.setValue(value);

        when(fortisApiClient.easyPinActivate(any(), any(), any(), any())).thenReturn(response);

        FortisProcessState fortisProcessState = new FortisProcessState();
        fortisProcessState.setLoginSessionId("TEST_LOGINSESSIONID");
        fortisProcessState.setOathTokenId("TEST_TOKEN_ID");
        fortisProcessState.setEncryptionKey("4948d55db654bdafb086b8aa42216cc4");
        fortisProcessState.setEncCredentials(
                "vIi4kU28kCSzVji40RoE6j8Ory0P4lZUj+Ki3nPOmJi0T0vKos69g9Hgqay/33s6TVT19z9HVEgNKr/D7LbJXFOj8crG4chNGSjYkBRTdfV7OnnD8HEPn9bw+SN3S5RpYcIG7wOi9/0Tmoah6rT2uZPtOCHZLlIbFBP5p80ynCs=");
        fortisProcessState.setSmsOtp("10101");
        fortisProcessState.setEnrollmentSessionId("12341234-1234-1234-1234-12341234");
        fortisProcessState.setCardFrameId("CARDFRAME_ID");
        fortisProcessState.setDeviceId("TEST_DEVICEID");

        FortisAuthData fortisAuthData = new FortisAuthData();
        fortisAuthData.setClientNumber("1012310123");

        AgentProceedNextStepAuthenticationRequest request =
                new AgentProceedNextStepAuthenticationRequest(
                        AgentAuthenticationProcessStepIdentifier.of("doesnt-matter"),
                        AgentAuthenticationProcessState.of(
                                "FortisProcessState",
                                objectMapper.writeValueAsString(fortisProcessState)),
                        AgentAuthenticationPersistedData.of(
                                "FortisAuthData", objectMapper.writeValueAsString(fortisAuthData)),
                        AgentExtendedClientInfo.builder().build());

        // when
        AgentAuthenticationResult result = step.execute(request);

        // then
        assertThat(result).isInstanceOf(AgentSucceededAuthenticationResult.class);

        assertAuthDataContainsValue(result, "cardFrameId", "CARDFRAME_ID");
        assertAuthDataContainsValue(result, "deviceId", "TEST_DEVICEID");
        assertAuthDataContainsValue(result, "oathTokenId", "TEST_TOKEN_ID");
        assertAuthDataContainsValue(
                result,
                "ocraKey",
                "dc203ea9c11f4857207f712a5eaaaf55a689590bc60adaec82860dfa059ae1c9ace0262728daabc42c7e8e1dbbe8763fc7005fc6049d0927c66671716b85a86e");
    }
}
