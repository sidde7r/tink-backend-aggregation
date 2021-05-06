package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.steps.otpverification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.security.KeyPair;
import java.security.Security;
import java.util.UUID;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.agentplatform.authentication.ObjectMapperFactory;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.ProcessDataUtil;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.rpc.tlc.device.OtpVerificationResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.persistance.MetroAuthenticationData;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.persistance.MetroDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.persistance.MetroProcessState;
import se.tink.backend.aggregation.agents.utils.crypto.EllipticCurve;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentUserInteractionAuthenticationProcessRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.AgentFieldValue;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.IncorrectOtpError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.ExternalApiCallResult;

@RunWith(MockitoJUnitRunner.class)
public class OtpVerificationStepTest {
    private static final String SUCCESS =
            "{\"error_code\":0,\"error_message\":\"\",\"data\":{\"assertion_error_code\":0,\"assertion_error_message\":\"\",\"state\":\"pending\",\"control_flow\":[{\"assertion_id\":\"ajOwK0zjWNc11BPCS5qC30/q\",\"assertions\":[{\"method\":\"pin\",\"last_used\":1612276670643,\"assertion_id\":\"Y/Jh5r1IagyOu7hXYKYZF/zZ\",\"status\":\"unregistered\",\"length\":6,\"history_enabled\":false}],\"type\":\"registration\"}],\"assertions_complete\":false},\"headers\":[]}";
    private static final String OTP = "123123";
    private static final String USER_ID = "123456789";
    private static final String ASSERTION_ID = "ASSERTION";
    private static final String CHALLENGE = "CHALLENGE";

    private static final String DEVICE_ID = UUID.randomUUID().toString();
    private static final String SESSION_ID = UUID.randomUUID().toString();

    private KeyPair keyPair;

    @Mock private OtpVerificationCall otpVerificationCall;

    private OtpVerificationStep authenticationStep;

    private ObjectMapper instance;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Before
    public void setUp() throws Exception {
        this.instance = new ObjectMapperFactory().getInstance();
        this.keyPair = EllipticCurve.generateKeyPair("prime256v1");
        MetroDataAccessorFactory metroDataAccessorFactory = new MetroDataAccessorFactory(instance);
        this.authenticationStep =
                new OtpVerificationStep(metroDataAccessorFactory, otpVerificationCall);
    }

    @Test
    public void shouldSetNextStepAsConfirmChallengeStep() throws IOException {
        // given
        AgentUserInteractionAuthenticationProcessRequest request = createFlowRequest(keyPair);
        when(otpVerificationCall.execute(any(), any(), any()))
                .thenReturn(
                        new ExternalApiCallResult<>(
                                instance.readValue(SUCCESS, OtpVerificationResponse.class)));

        // when
        AgentAuthenticationResult result = authenticationStep.execute(request);

        // then
        assertThat(result).isInstanceOf(AgentProceedNextStepAuthenticationResult.class);
        assertThat(result.getAuthenticationProcessStepIdentifier().get().getValue())
                .isEqualTo("DeviceRegistrationChallengeStep");
    }

    @Test
    public void shouldReturnAgentFailedAuthenticationResultWhenCallHasFailed() {
        // given
        AgentUserInteractionAuthenticationProcessRequest request = createFlowRequest(keyPair);
        when(otpVerificationCall.execute(any(), any(), any()))
                .thenReturn(new ExternalApiCallResult<>(new IncorrectOtpError()));

        // when
        AgentAuthenticationResult result = authenticationStep.execute(request);

        // then
        assertThat(result).isInstanceOf(AgentFailedAuthenticationResult.class);
        assertThat(result.getAuthenticationProcessStepIdentifier()).isEmpty();
    }

    private AgentUserInteractionAuthenticationProcessRequest createFlowRequest(
            KeyPair requestSignatureECKeyPair) {
        return ProcessDataUtil.userInteractionAuthRequest(
                () ->
                        new MetroProcessState()
                                .setAssertionId(ASSERTION_ID)
                                .setChallenge(CHALLENGE)
                                .setSessionId(SESSION_ID),
                () ->
                        new MetroAuthenticationData()
                                .setSignatureESKeyPair(requestSignatureECKeyPair)
                                .setUserId(USER_ID)
                                .setDeviceId(DEVICE_ID),
                () -> new AgentFieldValue(Key.OTP_INPUT.getFieldKey(), OTP));
    }
}
