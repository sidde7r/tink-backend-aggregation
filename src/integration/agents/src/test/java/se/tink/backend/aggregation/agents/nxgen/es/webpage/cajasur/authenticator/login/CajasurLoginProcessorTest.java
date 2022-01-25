package se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.authenticator.login;

import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.CajasurSessionState;
import se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.authenticator.CajasurAuthenticationApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@RunWith(MockitoJUnitRunner.class)
public class CajasurLoginProcessorTest {

    private static final String USERNAME = "dummyTestUsername";
    private static final String PASSWORD = "dummyTestPassword";

    @Mock private CajasurAuthenticationApiClient apiClient;

    @Mock private BufferedImage passwordVirtualKeyboardImage;

    @Mock private Credentials credentials;

    private SessionStorage sessionStorage = new SessionStorage();

    private CajasurLoginProcessor objectUnderTest;

    @Before
    public void init() {
        objectUnderTest = new CajasurLoginProcessor(apiClient, sessionStorage);
        when(credentials.getField(Field.Key.USERNAME)).thenReturn(USERNAME);
        when(credentials.getField(Field.Key.PASSWORD)).thenReturn(PASSWORD);
        when(apiClient.callForPasswordVirtualKeyboardImage())
                .thenReturn(passwordVirtualKeyboardImage);
    }

    @Test
    public void shouldExecuteLoginAndStoreResponse() {
        // given
        final String segmentId = "dummyTestSegmentId";
        final String encryptedObfuscatedLoginJs = "dummyTestEncryptedObfuscatedLoginJs";
        final String loginResponse = "dummyTestLoginResponse";
        when(apiClient.callForSegmentId()).thenReturn(segmentId);
        when(apiClient.callForEncryptObfuscatedLoginJS()).thenReturn(encryptedObfuscatedLoginJs);
        when(apiClient.callLogin(
                        new LoginRequestParams(
                                USERNAME,
                                PASSWORD,
                                segmentId,
                                encryptedObfuscatedLoginJs,
                                passwordVirtualKeyboardImage)))
                .thenReturn(loginResponse);

        // when
        AuthenticationStepResponse result = objectUnderTest.process(credentials);

        // then
        Assertions.assertThat(CajasurSessionState.getInstance(sessionStorage).getLoginResponse())
                .isEqualTo(loginResponse);
        Assertions.assertThat(result.isAuthenticationFinished()).isFalse();
        Assertions.assertThat(result.getSupplementInformationRequester()).isEmpty();
    }
}
