package se.tink.backend.aggregation.nxgen.controllers.authentication;

import com.google.common.collect.Lists;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.progressive.ProgressiveAuthAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationControllerImpl;

public class ProgressiveLoginExecutorTest {

    private ProgressiveAuthAgent agent;
    private SupplementalInformationController supplementalInformationController;
    private Credentials credentials;
    private ProgressiveLoginExecutor objectUnderTest;

    @Before
    public void init() {
        agent = Mockito.mock(ProgressiveAuthAgent.class);
        supplementalInformationController =
                Mockito.mock(SupplementalInformationControllerImpl.class);
        credentials = Mockito.mock(Credentials.class);
        objectUnderTest = new ProgressiveLoginExecutor(supplementalInformationController, agent);
    }

    @Test
    public void shouldRequestForSupplementField() throws Exception {
        // given
        final String stepId = "stepId";
        Field requestedField = Mockito.mock(Field.class);
        Map<String, String> callbackValue = new HashMap<>();
        callbackValue.put("fieldKey", "fieldValue");
        Mockito.when(supplementalInformationController.askSupplementalInformation(requestedField))
                .thenReturn(callbackValue);
        SupplementInformationRequester requester =
                new SupplementInformationRequester.Builder()
                        .withFields(Lists.newArrayList(requestedField))
                        .build();
        SteppableAuthenticationResponse steppableAuthenticationResponse =
                SteppableAuthenticationResponse.intermediateResponse(stepId, requester);
        Mockito.when(
                        agent.login(
                                Mockito.eq(
                                        SteppableAuthenticationRequest.initialRequest(
                                                credentials))))
                .thenReturn(steppableAuthenticationResponse);
        AuthenticationRequest authenticationRequest =
                new AuthenticationRequest(credentials).withUserInputs(callbackValue);
        Mockito.when(
                        agent.login(
                                Mockito.eq(
                                        SteppableAuthenticationRequest.subsequentRequest(
                                                stepId, authenticationRequest))))
                .thenReturn(SteppableAuthenticationResponse.finalResponse());
        // when
        objectUnderTest.login(credentials);
        // then
        Mockito.verify(supplementalInformationController)
                .askSupplementalInformation(requestedField);
    }

    @Test
    public void shouldRequestForThirdPartyAppAndWait() throws Exception {
        // given
        final String stepId = "stepId";
        final String waitRequestKey = "waitRequestKey";
        final long waitLong = 10;
        Map<String, String> callbackValue = new HashMap<>();
        callbackValue.put("key", "value");
        ThirdPartyAppAuthenticationPayload thirdPartyAppAuthenticationPayload =
                Mockito.mock(ThirdPartyAppAuthenticationPayload.class);
        SupplementalWaitRequest supplementalWaitRequest =
                new SupplementalWaitRequest(waitRequestKey, waitLong, TimeUnit.MINUTES);
        Mockito.when(
                        supplementalInformationController.waitForSupplementalInformation(
                                waitRequestKey, waitLong, TimeUnit.MINUTES))
                .thenReturn(Optional.of(callbackValue));
        SupplementInformationRequester requester =
                new SupplementInformationRequester.Builder()
                        .withThirdPartyAppAuthenticationPayload(thirdPartyAppAuthenticationPayload)
                        .withSupplementalWaitRequest(supplementalWaitRequest)
                        .build();

        SteppableAuthenticationResponse steppableAuthenticationResponse =
                SteppableAuthenticationResponse.intermediateResponse(stepId, requester);
        Mockito.when(
                        agent.login(
                                Mockito.eq(
                                        SteppableAuthenticationRequest.initialRequest(
                                                credentials))))
                .thenReturn(steppableAuthenticationResponse);

        AuthenticationRequest authenticationRequest =
                new AuthenticationRequest(credentials).withCallbackData(callbackValue);
        Mockito.when(
                        agent.login(
                                Mockito.eq(
                                        SteppableAuthenticationRequest.subsequentRequest(
                                                stepId, authenticationRequest))))
                .thenReturn(SteppableAuthenticationResponse.finalResponse());
        // when
        objectUnderTest.login(credentials);
        // then
        Mockito.verify(supplementalInformationController)
                .openThirdPartyApp(thirdPartyAppAuthenticationPayload);
        Mockito.verify(supplementalInformationController)
                .waitForSupplementalInformation(waitRequestKey, waitLong, TimeUnit.MINUTES);
    }

    @Test
    public void shouldRequestForThirdPartyApp() throws Exception {
        // given
        final String stepId = "stepId";
        Map<String, String> callbackValue = new HashMap<>();
        ThirdPartyAppAuthenticationPayload thirdPartyAppAuthenticationPayload =
                Mockito.mock(ThirdPartyAppAuthenticationPayload.class);
        SupplementInformationRequester requester =
                new SupplementInformationRequester.Builder()
                        .withThirdPartyAppAuthenticationPayload(thirdPartyAppAuthenticationPayload)
                        .build();

        SteppableAuthenticationResponse steppableAuthenticationResponse =
                SteppableAuthenticationResponse.intermediateResponse(stepId, requester);
        Mockito.when(
                        agent.login(
                                Mockito.eq(
                                        SteppableAuthenticationRequest.initialRequest(
                                                credentials))))
                .thenReturn(steppableAuthenticationResponse);

        AuthenticationRequest authenticationRequest = new AuthenticationRequest(credentials);
        Mockito.when(
                        agent.login(
                                Mockito.eq(
                                        SteppableAuthenticationRequest.subsequentRequest(
                                                stepId, authenticationRequest))))
                .thenReturn(SteppableAuthenticationResponse.finalResponse());
        // when
        objectUnderTest.login(credentials);
        // then
        Mockito.verify(supplementalInformationController)
                .openThirdPartyApp(thirdPartyAppAuthenticationPayload);
    }

    @Test
    public void shouldRequestForWait() throws Exception {
        // given
        final String stepId = "stepId";
        final String waitRequestKey = "waitRequestKey";
        final long waitLong = 10;
        Map<String, String> callbackValue = new HashMap<>();
        callbackValue.put("key", "value");
        SupplementalWaitRequest supplementalWaitRequest =
                new SupplementalWaitRequest(waitRequestKey, waitLong, TimeUnit.MINUTES);
        Mockito.when(
                        supplementalInformationController.waitForSupplementalInformation(
                                waitRequestKey, waitLong, TimeUnit.MINUTES))
                .thenReturn(Optional.of(callbackValue));
        SupplementInformationRequester requester =
                new SupplementInformationRequester.Builder()
                        .withSupplementalWaitRequest(supplementalWaitRequest)
                        .build();

        SteppableAuthenticationResponse steppableAuthenticationResponse =
                SteppableAuthenticationResponse.intermediateResponse(stepId, requester);
        Mockito.when(
                        agent.login(
                                Mockito.eq(
                                        SteppableAuthenticationRequest.initialRequest(
                                                credentials))))
                .thenReturn(steppableAuthenticationResponse);

        AuthenticationRequest authenticationRequest =
                new AuthenticationRequest(credentials).withCallbackData(callbackValue);
        Mockito.when(
                        agent.login(
                                Mockito.eq(
                                        SteppableAuthenticationRequest.subsequentRequest(
                                                stepId, authenticationRequest))))
                .thenReturn(SteppableAuthenticationResponse.finalResponse());
        // when
        objectUnderTest.login(credentials);
        // then
        Mockito.verify(supplementalInformationController)
                .waitForSupplementalInformation(waitRequestKey, waitLong, TimeUnit.MINUTES);
    }
}
