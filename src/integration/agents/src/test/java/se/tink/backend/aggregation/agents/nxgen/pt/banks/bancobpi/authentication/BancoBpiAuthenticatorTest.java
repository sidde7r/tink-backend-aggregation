package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.authentication;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SteppableAuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.AutomaticAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.OtpStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.PinCodeGeneratorAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.UsernamePasswordAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationFormer;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class BancoBpiAuthenticatorTest {

    private static final String STATE_ACTIVATED =
            "{\"accessPin\":\"2990\",\"deviceUUID\":\"1672b219-826a-4d81-b77c-96dc7625239f\",\"deviceActivationFinished\":true}";
    private static final String STATE_WAITING_FOR_OTP =
            "{\"accessPin\":\"2990\",\"deviceUUID\":\"1672b219-826a-4d81-b77c-96dc7625239f\",\"deviceActivationFinished\":false}";
    private static final String STATE_TO_ACTIVATE = "{}";
    private static final String USER_STATE_KEY = "BancoBpiUserState";

    private TinkHttpClient httpClient;
    private SupplementalInformationFormer supplementalInformationFormer;
    private SessionStorage sessionStorage;

    @Before
    public void init() {
        httpClient = Mockito.mock(TinkHttpClient.class);
        supplementalInformationFormer = Mockito.mock(SupplementalInformationFormer.class);
        sessionStorage = Mockito.mock(SessionStorage.class);
    }

    @Test
    public void shouldInitWithManualAuthenticationStepChain()
            throws AuthenticationException, AuthorizationException {
        // given
        Mockito.when(sessionStorage.get(USER_STATE_KEY)).thenReturn(STATE_TO_ACTIVATE);
        // when
        BancoBpiAuthenticator objectUnderTest =
                new BancoBpiAuthenticator(
                        httpClient, supplementalInformationFormer, sessionStorage);
        List<? extends AuthenticationStep> steps =
                ImmutableList.copyOf(objectUnderTest.authenticationSteps());
        // then
        Assert.assertEquals(3, steps.size());
        Assert.assertTrue(
                objectUnderTest.isManualAuthentication(Mockito.mock(CredentialsRequest.class)));
        Assert.assertEquals(UsernamePasswordAuthenticationStep.class, steps.get(0).getClass());
        Assert.assertEquals(PinCodeGeneratorAuthenticationStep.class, steps.get(1).getClass());
        Assert.assertEquals(OtpStep.class, steps.get(2).getClass());
    }

    @Test
    public void shouldInitWithAutoAuthenticationStepChain()
            throws AuthenticationException, AuthorizationException {
        // given
        Mockito.when(sessionStorage.get(USER_STATE_KEY)).thenReturn(STATE_ACTIVATED);
        // when
        BancoBpiAuthenticator objectUnderTest =
                new BancoBpiAuthenticator(
                        httpClient, supplementalInformationFormer, sessionStorage);
        List<? extends AuthenticationStep> steps =
                ImmutableList.copyOf(objectUnderTest.authenticationSteps());
        // then
        Assert.assertEquals(1, steps.size());
        Assert.assertFalse(
                objectUnderTest.isManualAuthentication(Mockito.mock(CredentialsRequest.class)));
        Assert.assertEquals(AutomaticAuthenticationStep.class, steps.get(0).getClass());
    }

    @Test
    public void processAuthenticationShouldSaveUserStateOnReturn()
            throws AuthenticationException, AuthorizationException {
        // given
        Mockito.when(supplementalInformationFormer.getField(Key.OTP_INPUT))
                .thenReturn(Mockito.mock(Field.class));
        Mockito.when(sessionStorage.get(USER_STATE_KEY)).thenReturn(STATE_WAITING_FOR_OTP);
        SteppableAuthenticationRequest request =
                SteppableAuthenticationRequest.subsequentRequest(
                        OtpStep.class.getName(),
                        new AuthenticationRequest(Mockito.mock(Credentials.class)));
        BancoBpiAuthenticator objectUnderTest =
                new BancoBpiAuthenticator(
                        httpClient, supplementalInformationFormer, sessionStorage);
        // when
        objectUnderTest.processAuthentication(request);
        // then
        Mockito.verify(sessionStorage).put(USER_STATE_KEY, STATE_WAITING_FOR_OTP);
    }
}
