package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.steps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.Security;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.AgentPlatformFortisApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.fields.OtpInputField;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.persistence.FortisAuthData;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.persistence.FortisDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.persistence.FortisProcessState;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.EasyPinProvisionResponse;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessState;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentUserInteractionAuthenticationProcessRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.AgentFieldValue;
import se.tink.backend.aggregation.agentsplatform.agentsframework.common.AgentExtendedClientInfo;

@RunWith(MockitoJUnitRunner.class)
public class EasyPinProvisionStepTest extends AbstractStepTest {

    @Mock private AgentPlatformFortisApiClient fortisApiClient;

    private final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

    private EasyPinProvisionStep step;

    static {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    @Before
    public void before() {
        step =
                new EasyPinProvisionStep(
                        fortisApiClient, new FortisDataAccessorFactory(objectMapper));
    }

    @Test
    public void shouldProvision() throws Exception {
        // given
        FortisProcessState fortisProcessState = new FortisProcessState();
        fortisProcessState.setEnrollmentSessionId(UUID.randomUUID().toString());
        fortisProcessState.setRegistrationCode("1234512345");

        FortisAuthData fortisAuthData = new FortisAuthData();
        fortisAuthData.setClientNumber("1012310123");

        AgentUserInteractionAuthenticationProcessRequest request =
                new AgentUserInteractionAuthenticationProcessRequest(
                        AgentAuthenticationProcessStepIdentifier.of("doesnt-matter"),
                        AgentAuthenticationPersistedData.of(
                                "FortisAuthData", objectMapper.writeValueAsString(fortisAuthData)),
                        AgentAuthenticationProcessState.of(
                                "FortisProcessState",
                                objectMapper.writeValueAsString(fortisProcessState)),
                        Collections.singletonList(new AgentFieldValue(OtpInputField.ID, "44444")),
                        AgentExtendedClientInfo.builder().build());

        when(fortisApiClient.easyPinProvision(any(), any(), any(), any()))
                .thenReturn(mockResponse());

        // when
        AgentAuthenticationResult result = step.execute(request);

        // then
        assertStepResult(result, EasyPinActivationStep.class);

        assertProcessStateContains(result, "encCredentials");
        assertProcessStateContains(result, "encryptionKey");

        verify(fortisApiClient)
                .easyPinProvision(
                        captor.capture(), captor.capture(), captor.capture(), captor.capture());

        List<String> captured = captor.getAllValues();
        assertNotNull(captured.get(0));
        assertEquals("1234512345", captured.get(1));
        assertEquals(fortisProcessState.getEnrollmentSessionId(), captured.get(2));
        assertEquals("1012310123", captured.get(3));
    }

    private EasyPinProvisionResponse mockResponse() {
        EasyPinProvisionResponse easyPinProvisionResponse = new EasyPinProvisionResponse();
        easyPinProvisionResponse.setMessage("SUCCESS");
        easyPinProvisionResponse.setStatus("0");
        easyPinProvisionResponse.setEncCredentials("123");
        return easyPinProvisionResponse;
    }
}
