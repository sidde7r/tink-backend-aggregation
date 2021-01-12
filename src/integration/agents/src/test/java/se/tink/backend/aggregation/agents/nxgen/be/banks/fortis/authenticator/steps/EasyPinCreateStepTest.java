package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.AgentPlatformFortisApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities.EasyPinCreateValueEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.fields.OtpInputField;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.fields.PhonenumberInputField;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.persistence.FortisDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.EasyPinCreateResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.helper.FortisRandomTokenGenerator;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessState;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentUserInteractionAuthenticationProcessRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.AgentFieldValue;

@RunWith(MockitoJUnitRunner.class)
public class EasyPinCreateStepTest extends AbstractStepTest {

    @Mock private AgentPlatformFortisApiClient fortisApiClient;

    @Mock private FortisRandomTokenGenerator fortisRandomTokenGenerator;

    private final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

    private EasyPinCreateStep step;

    @Before
    public void before() {
        step =
                new EasyPinCreateStep(
                        fortisApiClient,
                        fortisRandomTokenGenerator,
                        new FortisDataAccessorFactory(objectMapper));
    }

    @Test
    public void shouldCreateAndRequestOTP() throws Exception {
        // given
        EasyPinCreateValueEntity easyPinCreateValueEntity = new EasyPinCreateValueEntity();
        easyPinCreateValueEntity.setEnrollmentSessionId("TEST_ENROLLMENT");
        easyPinCreateValueEntity.setTokenId("TEST_TOKENID");
        easyPinCreateValueEntity.setRegistrationCode("TEST_REGISTRATION_CODE");
        EasyPinCreateResponse easyPinCreateResponse = new EasyPinCreateResponse();
        easyPinCreateResponse.setValue(easyPinCreateValueEntity);

        when(fortisRandomTokenGenerator.generateDeviceId()).thenReturn("TEST_DEVICEID");
        when(fortisApiClient.easyPinCreate(any(), any())).thenReturn(easyPinCreateResponse);

        AgentUserInteractionAuthenticationProcessRequest request =
                new AgentUserInteractionAuthenticationProcessRequest(
                        AgentAuthenticationProcessStepIdentifier.of("doesnt-matter"),
                        AgentAuthenticationPersistedData.of("FortisAuthData", "{}"),
                        AgentAuthenticationProcessState.of("FortisProcessState", "{}"),
                        Collections.singletonList(
                                new AgentFieldValue(PhonenumberInputField.ID, "+3212341234")));

        // when
        AgentAuthenticationResult result = step.execute(request);

        // then
        assertStepResult(result, EasyPinProvisionStep.class);

        assertProcessStateContainsValue(result, "deviceId", "TEST_DEVICEID");
        assertProcessStateContainsValue(result, "oathTokenId", "TEST_TOKENID");
        assertProcessStateContainsValue(result, "registrationCode", "TEST_REGISTRATION_CODE");
        assertProcessStateContainsValue(result, "enrollmentSessionId", "TEST_ENROLLMENT");

        assertFieldRequested(result, OtpInputField.ID);

        verify(fortisApiClient).easyPinCreate(captor.capture(), captor.capture());

        List<String> captured = captor.getAllValues();
        assertThat(captured.get(0)).isEqualTo("+3212341234");
        assertThat(captured.get(1)).isEqualTo("TEST_DEVICEID");
    }
}
