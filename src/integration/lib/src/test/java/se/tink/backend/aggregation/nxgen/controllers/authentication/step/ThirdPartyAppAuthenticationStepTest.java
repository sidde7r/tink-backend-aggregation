package se.tink.backend.aggregation.nxgen.controllers.authentication.step;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SupplementInformationRequester;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SupplementalWaitRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;

public class ThirdPartyAppAuthenticationStepTest {

    private ThirdPartyAppAuthenticationPayload payload;
    private SupplementalWaitRequest waitRequest;

    @Before
    public void init() {
        payload = Mockito.mock(ThirdPartyAppAuthenticationPayload.class);
        waitRequest = new SupplementalWaitRequest("key", 5, TimeUnit.MINUTES);
    }

    @Test
    public void shouldReturnSupplementInformationRequesterFromProvideObject()
            throws AuthenticationException, AuthorizationException {
        // given
        ThirdPartyAppRequestParamsProvider thirdPartyAppRequestParamsProvider =
                Mockito.mock(ThirdPartyAppRequestParamsProvider.class);
        Mockito.when(thirdPartyAppRequestParamsProvider.getPayload()).thenReturn(payload);
        Mockito.when(thirdPartyAppRequestParamsProvider.getWaitingConfiguration())
                .thenReturn(waitRequest);
        ThirdPartyAppAuthenticationStep objectUnderTest =
                new ThirdPartyAppAuthenticationStep(
                        thirdPartyAppRequestParamsProvider, (callbackData) -> {});
        // when
        Optional<SupplementInformationRequester> result =
                objectUnderTest.execute(new AuthenticationRequest(Mockito.mock(Credentials.class)));
        // then
        Assert.assertEquals(payload, result.get().getThirdPartyAppPayload().get());
        Assert.assertEquals(waitRequest, result.get().getSupplementalWaitRequest().get());
    }

    @Test
    public void shouldReturnSupplementInformationRequesterPassedDirectlyToConstructor()
            throws AuthenticationException, AuthorizationException {
        // given
        ThirdPartyAppAuthenticationStep objectUnderTest =
                new ThirdPartyAppAuthenticationStep(payload, waitRequest, (callbackData) -> {});
        // when
        Optional<SupplementInformationRequester> result =
                objectUnderTest.execute(new AuthenticationRequest(Mockito.mock(Credentials.class)));
        // then
        Assert.assertEquals(payload, result.get().getThirdPartyAppPayload().get());
        Assert.assertEquals(waitRequest, result.get().getSupplementalWaitRequest().get());
    }

    @Test
    public void shouldExecuteCallbackProcessor()
            throws AuthenticationException, AuthorizationException {
        // given
        AuthenticationRequest authenticationRequest =
                new AuthenticationRequest(Mockito.mock(Credentials.class));
        Map<String, String> callbackData = new HashMap<>();
        callbackData.put("callbackKey", "callbackValue");
        authenticationRequest.withCallbackData(callbackData);
        CallbackProcessorMultiData callbackProcessor =
                Mockito.mock(CallbackProcessorMultiData.class);
        ThirdPartyAppAuthenticationStep objectUnderTest =
                new ThirdPartyAppAuthenticationStep(payload, waitRequest, callbackProcessor);
        // when
        Optional<SupplementInformationRequester> result =
                objectUnderTest.execute(authenticationRequest);
        // then
        Mockito.verify(callbackProcessor).process(callbackData);
        Assert.assertFalse(result.isPresent());
    }
}
