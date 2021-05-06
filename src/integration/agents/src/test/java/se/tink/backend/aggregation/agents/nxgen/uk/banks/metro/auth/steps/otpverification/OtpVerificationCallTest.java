package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.steps.otpverification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import agents_platform_agents_framework.org.springframework.http.HttpStatus;
import agents_platform_agents_framework.org.springframework.http.ResponseEntity;
import java.security.KeyPair;
import java.security.Security;
import java.util.UUID;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.agentplatform.authentication.ObjectMapperFactory;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.ProcessDataUtil;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.entity.UserIdHeaderEntity;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.entity.tlc.asserts.ActionType;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.entity.tlc.asserts.AssertionEntity;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.entity.tlc.asserts.AssertionType;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.entity.tlc.asserts.MethodType;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.error.UnknownError;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.rpc.tlc.asserts.ConfirmChallengeRequest;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.rpc.tlc.device.OtpVerificationResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.persistance.MetroAuthenticationData;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.persistance.MetroProcessState;
import se.tink.backend.aggregation.agents.utils.crypto.EllipticCurve;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AccountBlockedError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.DeviceRegistrationError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.IncorrectOtpError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.AgentHttpClient;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.AuthenticationPersistedDataCookieStoreAccessorFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.ExternalApiCallResult;

@RunWith(MockitoJUnitRunner.class)
public class OtpVerificationCallTest {
    private static final ResponseEntity<String> SUCCESS =
            ResponseEntity.status(HttpStatus.OK)
                    .body(
                            "{\"error_code\":0,\"error_message\":\"\",\"data\":{\"assertion_error_code\":0,\"assertion_error_message\":\"\",\"state\":\"pending\",\"control_flow\":[{\"assertion_id\":\"ajOwK0zjWNc11BPCS5qC30/q\",\"assertions\":[{\"method\":\"pin\",\"last_used\":1612276670643,\"assertion_id\":\"Y/Jh5r1IagyOu7hXYKYZF/zZ\",\"status\":\"unregistered\",\"length\":6,\"history_enabled\":false}],\"type\":\"registration\"}],\"assertions_complete\":false},\"headers\":[]}");

    private static final ResponseEntity<String> TOO_MANY_REGISTERED_DEVICES =
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(
                            "{\"error_code\":4001,\"error_message\":\"Session rejected'e3fd6280-9785-409b-a927-a32851ac8e53'\",\"data\":{\"rejection_data\":null,\"state\":\"rejected\",\"assertions_complete\":false,\"failure_data\":{\"source\":{\"action_type\":\"reject\",\"type\":\"action\"},\"reason\":{\"type\":\"assertion_rejected\",\"data\":{\"device_bind_count_exceeded\":true,\"device_management_session_required\":false}}}},\"headers\":[]}");

    private static final ResponseEntity<String> ACCOUNT_BLOCKED =
            ResponseEntity.status(HttpStatus.OK)
                    .body(
                            "{\"error_code\":0,\"error_message\":\"\",\"data\":{\"assertion_error_code\":0,\"assertion_error_message\":\"\",\"data\":{\"retries_left\":0,\"account_locked\":true}},\"headers\":[]}");

    private static final ResponseEntity<String> INVALID_OTP =
            ResponseEntity.status(HttpStatus.OK)
                    .body(
                            "{\"error_code\":7,\"error_message\":\"\",\"data\":{\"assertion_error_code\":0,\"assertion_error_message\":\"\",\"data\":{\"retries_left\":0,\"account_locked\":false}},\"headers\":[]}");

    private static final ResponseEntity<String> UNKNOWN_ERROR =
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{}");

    private static final String DEVICE_ID = UUID.randomUUID().toString();

    private static final String SESSION_ID = UUID.randomUUID().toString();

    private KeyPair keyPair;

    @Mock private AgentHttpClient httpClient;

    private OtpVerificationCall call;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Before
    public void setUp() throws Exception {
        ObjectMapperFactory objectMapperFactory = new ObjectMapperFactory();
        this.keyPair = EllipticCurve.generateKeyPair("prime256v1");
        this.call = new OtpVerificationCall(httpClient, objectMapperFactory.getInstance());
    }

    @Test
    public void shouldReturnProperValue() {
        // given
        OtpVerificationParameters parameters =
                new OtpVerificationParameters(
                        SESSION_ID, DEVICE_ID, keyPair.getPrivate(), buildRequest());
        AgentProceedNextStepAuthenticationRequest request =
                ProcessDataUtil.nextStepAuthRequest(
                        MetroProcessState::new, MetroAuthenticationData::new);
        when(httpClient.exchange(any(), eq(String.class), any())).thenReturn(SUCCESS);
        // when
        ExternalApiCallResult<OtpVerificationResponse> execute =
                call.execute(
                        parameters,
                        request.getAgentExtendedClientInfo(),
                        AuthenticationPersistedDataCookieStoreAccessorFactory.create(
                                request.getAuthenticationPersistedData()));

        // then
        assertThat(execute.getResponse()).isNotEmpty();
        assertThat(execute.getResponse().get().getAssertionId())
                .isEqualTo("Y/Jh5r1IagyOu7hXYKYZF/zZ");
    }

    @Test
    public void shouldCatchIncorrectOtp() {
        // given
        OtpVerificationParameters parameters =
                new OtpVerificationParameters(
                        SESSION_ID, DEVICE_ID, keyPair.getPrivate(), buildRequest());
        AgentProceedNextStepAuthenticationRequest request =
                ProcessDataUtil.nextStepAuthRequest(
                        MetroProcessState::new, MetroAuthenticationData::new);
        when(httpClient.exchange(any(), eq(String.class), any())).thenReturn(INVALID_OTP);

        // when
        ExternalApiCallResult<OtpVerificationResponse> execute =
                call.execute(
                        parameters,
                        request.getAgentExtendedClientInfo(),
                        AuthenticationPersistedDataCookieStoreAccessorFactory.create(
                                request.getAuthenticationPersistedData()));

        // then
        assertThat(execute.getResponse()).isEmpty();
        assertThat(execute.getAgentBankApiError()).isNotEmpty();
        assertThat(execute.getAgentBankApiError().get()).isInstanceOf(IncorrectOtpError.class);
    }

    @Test
    public void shouldCatchBlockedAccount() {
        // given
        OtpVerificationParameters parameters =
                new OtpVerificationParameters(
                        SESSION_ID, DEVICE_ID, keyPair.getPrivate(), buildRequest());
        AgentProceedNextStepAuthenticationRequest request =
                ProcessDataUtil.nextStepAuthRequest(
                        MetroProcessState::new, MetroAuthenticationData::new);
        when(httpClient.exchange(any(), eq(String.class), any())).thenReturn(ACCOUNT_BLOCKED);

        // when
        ExternalApiCallResult<OtpVerificationResponse> execute =
                call.execute(
                        parameters,
                        request.getAgentExtendedClientInfo(),
                        AuthenticationPersistedDataCookieStoreAccessorFactory.create(
                                request.getAuthenticationPersistedData()));

        // then
        assertThat(execute.getResponse()).isEmpty();
        assertThat(execute.getAgentBankApiError()).isNotEmpty();
        assertThat(execute.getAgentBankApiError().get()).isInstanceOf(AccountBlockedError.class);
    }

    @Test
    public void shouldCatchTooManyDevices() {
        // given
        OtpVerificationParameters parameters =
                new OtpVerificationParameters(
                        SESSION_ID, DEVICE_ID, keyPair.getPrivate(), buildRequest());
        AgentProceedNextStepAuthenticationRequest request =
                ProcessDataUtil.nextStepAuthRequest(
                        MetroProcessState::new, MetroAuthenticationData::new);
        when(httpClient.exchange(any(), eq(String.class), any()))
                .thenReturn(TOO_MANY_REGISTERED_DEVICES);

        // when
        ExternalApiCallResult<OtpVerificationResponse> execute =
                call.execute(
                        parameters,
                        request.getAgentExtendedClientInfo(),
                        AuthenticationPersistedDataCookieStoreAccessorFactory.create(
                                request.getAuthenticationPersistedData()));

        // then
        assertThat(execute.getResponse()).isEmpty();
        assertThat(execute.getAgentBankApiError()).isNotEmpty();
        assertThat(execute.getAgentBankApiError().get())
                .isInstanceOf(DeviceRegistrationError.class);
    }

    @Test
    public void shouldCatchUnknownError() {
        // given
        OtpVerificationParameters parameters =
                new OtpVerificationParameters(
                        SESSION_ID, DEVICE_ID, keyPair.getPrivate(), buildRequest());
        AgentProceedNextStepAuthenticationRequest request =
                ProcessDataUtil.nextStepAuthRequest(
                        MetroProcessState::new, MetroAuthenticationData::new);
        when(httpClient.exchange(any(), eq(String.class), any())).thenReturn(UNKNOWN_ERROR);

        // when
        ExternalApiCallResult<OtpVerificationResponse> execute =
                call.execute(
                        parameters,
                        request.getAgentExtendedClientInfo(),
                        AuthenticationPersistedDataCookieStoreAccessorFactory.create(
                                request.getAuthenticationPersistedData()));

        // then
        assertThat(execute.getResponse()).isEmpty();
        assertThat(execute.getAgentBankApiError()).isNotEmpty();
        assertThat(execute.getAgentBankApiError().get()).isInstanceOf(UnknownError.class);
    }

    private ConfirmChallengeRequest buildRequest() {
        return new ConfirmChallengeRequest(
                new UserIdHeaderEntity(""),
                AssertionEntity.builder()
                        .action(ActionType.AUTHENTICATION)
                        .method(MethodType.OTP)
                        .assertionType(AssertionType.AUTHENTICATE)
                        .build());
    }
}
