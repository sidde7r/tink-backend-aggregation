package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.authentication;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.BancoBpiAuthContext;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.BancoBpiEntityManager;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.AutomaticAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.OtpStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.PinCodeGeneratorAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.UsernamePasswordAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationFormer;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;

public class BancoBpiAuthenticatorTest {

    private static final String STATE_ACTIVATED =
            "{\"accessPin\":\"2990\",\"deviceUUID\":\"1672b219-826a-4d81-b77c-96dc7625239f\",\"deviceActivationFinished\":true}";
    private static final String STATE_WAITING_FOR_OTP =
            "{\"accessPin\":\"2990\",\"deviceUUID\":\"1672b219-826a-4d81-b77c-96dc7625239f\",\"deviceActivationFinished\":false}";
    private static final String STATE_TO_ACTIVATE = "{}";
    private static final String USER_STATE_KEY = "BancoBpiUserState";

    private TinkHttpClient httpClient;
    private SupplementalInformationFormer supplementalInformationFormer;
    private BancoBpiEntityManager entityManager;
    private BancoBpiAuthContext userState;
    private BancoBpiAuthenticator objectUnderTest;

    @Before
    public void init() {
        httpClient = Mockito.mock(TinkHttpClient.class);
        supplementalInformationFormer = Mockito.mock(SupplementalInformationFormer.class);
        entityManager = Mockito.mock(BancoBpiEntityManager.class);
        userState = Mockito.mock(BancoBpiAuthContext.class);
        Mockito.when(entityManager.getAuthContext()).thenReturn(userState);
        objectUnderTest =
                new BancoBpiAuthenticator(httpClient, supplementalInformationFormer, entityManager);
    }

    @Test
    public void shouldInitWithManualAuthenticationStepChain()
            throws AuthenticationException, AuthorizationException {
        // given
        Mockito.when(userState.isDeviceActivationFinished()).thenReturn(false);
        // when

        List<AuthenticationStep> steps =
                ImmutableList.copyOf(objectUnderTest.authenticationSteps());
        // then
        Assert.assertEquals(4, steps.size());
        Assert.assertEquals(ModuleVersionAuthenticationStep.class, steps.get(0).getClass());
        Assert.assertEquals(UsernamePasswordAuthenticationStep.class, steps.get(1).getClass());
        Assert.assertEquals(PinCodeGeneratorAuthenticationStep.class, steps.get(2).getClass());
        Assert.assertEquals(OtpStep.class, steps.get(3).getClass());
    }

    @Test
    public void shouldInitWithAutoAuthenticationStepChain()
            throws AuthenticationException, AuthorizationException {
        // given
        Mockito.when(userState.isDeviceActivationFinished()).thenReturn(true);
        // when
        List<AuthenticationStep> steps =
                ImmutableList.copyOf(objectUnderTest.authenticationSteps());
        // then
        Assert.assertEquals(2, steps.size());
        Assert.assertEquals(ModuleVersionAuthenticationStep.class, steps.get(0).getClass());
        Assert.assertEquals(AutomaticAuthenticationStep.class, steps.get(1).getClass());
    }
}
