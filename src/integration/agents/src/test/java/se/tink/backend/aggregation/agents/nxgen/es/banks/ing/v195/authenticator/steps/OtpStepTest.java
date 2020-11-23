package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.rpc.ErrorCodeMessage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class OtpStepTest {

    private static final String LOGIN_PROCESS_ID = "123";

    private OtpStep otpStep;
    private IngApiClient apiClient;
    private SupplementalInformationHelper supplementalInformationHelper;

    @Before
    public void setUp() {
        SessionStorage sessionStorage = new SessionStorage();
        sessionStorage.put(Storage.LOGIN_PROCESS_ID, LOGIN_PROCESS_ID);

        apiClient = mock(IngApiClient.class);
        supplementalInformationHelper = mock(SupplementalInformationHelper.class);
        otpStep = new OtpStep(apiClient, sessionStorage, null, supplementalInformationHelper);
    }

    @Test
    public void testWrongSmsCode() {
        // given
        when(supplementalInformationHelper.waitForOtpInput()).thenReturn("SMS_OTP_CODE");
        HttpResponseException httpResponseException =
                new HttpResponseException("", null, get403WrongOtpResponse());
        when(apiClient.putLoginRestSession(eq("SMS_OTP_CODE"), eq(LOGIN_PROCESS_ID)))
                .thenThrow(httpResponseException);

        // when
        Throwable throwable =
                catchThrowable(() -> otpStep.execute(new AuthenticationRequest(null)));

        // then
        assertThat(throwable).isInstanceOf(LoginException.class);
        assertThat(((LoginException) throwable).getError())
                .isEqualTo(LoginError.INCORRECT_CHALLENGE_RESPONSE);
    }

    private HttpResponse get403WrongOtpResponse() {
        HttpResponse mockedResponse = mock(HttpResponse.class);
        when(mockedResponse.getStatus()).thenReturn(403);
        when(mockedResponse.getBody(any()))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                " {\"message\":\"C?digo SMS incorrecto\",\"errorCode\":403005}",
                                ErrorCodeMessage.class));
        return mockedResponse;
    }
}
