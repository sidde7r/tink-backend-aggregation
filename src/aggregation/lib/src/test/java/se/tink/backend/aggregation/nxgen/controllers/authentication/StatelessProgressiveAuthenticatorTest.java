package se.tink.backend.aggregation.nxgen.controllers.authentication;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.SteppableAuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.SteppableAuthenticationResponse;

public class StatelessProgressiveAuthenticatorTest {

    private Credentials credentials;

    @Before
    public void init() {
        credentials = Mockito.mock(Credentials.class);
    }

    @Test
    public void shouldIterateOverAuthenticationSteps()
            throws AuthenticationException, AuthorizationException {
        // given
        AuthenticationStep step1 =
                mockAuthenticationStep("step1", AuthenticationStepResponse.executeNextStep(), null);
        AuthenticationStep step2 =
                mockAuthenticationStep("step2", AuthenticationStepResponse.executeNextStep(), null);
        StatelessProgressiveAuthenticator objectUnderTest = createObjectUnderTest(step1, step2);
        // when
        SteppableAuthenticationResponse result =
                objectUnderTest.processAuthentication(
                        SteppableAuthenticationRequest.initialRequest(credentials));
        // then
        Assert.assertFalse(result.getStepIdentifier().isPresent());
        Mockito.verify(step1).execute(Mockito.any());
        Mockito.verify(step2).execute(Mockito.any());
    }

    @Test
    public void shouldExecuteStepWithRequestedIdFromPreviousStep()
            throws AuthenticationException, AuthorizationException {
        // given
        AuthenticationStep step1 =
                mockAuthenticationStep(
                        "step1", AuthenticationStepResponse.executeStepWithId("step3"), null);
        AuthenticationStep stepToOmit =
                mockAuthenticationStep("step2", AuthenticationStepResponse.executeNextStep(), null);
        AuthenticationStep step3 =
                mockAuthenticationStep("step3", AuthenticationStepResponse.executeNextStep(), null);
        StatelessProgressiveAuthenticator objectUnderTest =
                createObjectUnderTest(step1, stepToOmit, step3);
        // when
        SteppableAuthenticationResponse result =
                objectUnderTest.processAuthentication(
                        SteppableAuthenticationRequest.initialRequest(credentials));
        // then
        Assert.assertFalse(result.getStepIdentifier().isPresent());
        Mockito.verify(step1).execute(Mockito.any());
        Mockito.verify(stepToOmit, Mockito.never()).execute(Mockito.any());
        Mockito.verify(step3).execute(Mockito.any());
    }

    @Test
    public void shouldSucceededAuthenticationOnDemand()
            throws AuthenticationException, AuthorizationException {
        // given
        AuthenticationStep step1 =
                mockAuthenticationStep(
                        "step1", AuthenticationStepResponse.authenticationSucceeded(), null);
        AuthenticationStep stepToOmit =
                mockAuthenticationStep("step2", AuthenticationStepResponse.executeNextStep(), null);
        StatelessProgressiveAuthenticator objectUnderTest =
                createObjectUnderTest(step1, stepToOmit);
        // when
        SteppableAuthenticationResponse result =
                objectUnderTest.processAuthentication(
                        SteppableAuthenticationRequest.initialRequest(credentials));
        // then
        Assert.assertFalse(result.getStepIdentifier().isPresent());
        Mockito.verify(step1).execute(Mockito.any());
        Mockito.verify(stepToOmit, Mockito.never()).execute(Mockito.any());
    }

    @Test
    public void shouldRequestForSupplementInformation()
            throws AuthenticationException, AuthorizationException {
        // given
        SupplementInformationRequester supplementInformationRequester =
                new SupplementInformationRequester.Builder()
                        .withFields(Lists.newArrayList(Mockito.mock(Field.class)))
                        .build();
        AuthenticationStep step1 =
                mockAuthenticationStep(
                        "step1",
                        AuthenticationStepResponse.requestForSupplementInformation(
                                supplementInformationRequester),
                        null);
        AuthenticationStep step2 =
                mockAuthenticationStep("step2", AuthenticationStepResponse.executeNextStep(), null);
        StatelessProgressiveAuthenticator objectUnderTest = createObjectUnderTest(step1, step2);
        // when
        SteppableAuthenticationResponse result =
                objectUnderTest.processAuthentication(
                        SteppableAuthenticationRequest.initialRequest(credentials));
        // then
        Assert.assertTrue(result.getStepIdentifier().isPresent());
        Assert.assertEquals("step1", result.getStepIdentifier().get());
        Mockito.verify(step1).execute(Mockito.any());
        Mockito.verify(step2, Mockito.never()).execute(Mockito.any());
    }

    @Test
    public void shouldExecuteStepWithRequestedIdInInput()
            throws AuthenticationException, AuthorizationException {
        // given
        SteppableAuthenticationRequest request =
                SteppableAuthenticationRequest.subsequentRequest(
                        "step2", new AuthenticationRequest(credentials));
        AuthenticationStep step1 =
                mockAuthenticationStep("step1", AuthenticationStepResponse.executeNextStep(), null);
        AuthenticationStep step2 =
                mockAuthenticationStep("step2", AuthenticationStepResponse.executeNextStep(), null);
        StatelessProgressiveAuthenticator objectUnderTest = createObjectUnderTest(step1, step2);
        // when
        SteppableAuthenticationResponse result = objectUnderTest.processAuthentication(request);
        // then
        Mockito.verify(step1, Mockito.never()).execute(Mockito.any());
        Mockito.verify(step2).execute(Mockito.any());
    }

    private AuthenticationStep mockAuthenticationStep(
            String stepId, AuthenticationStepResponse response, AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        AuthenticationStep step = Mockito.mock(AuthenticationStep.class);
        Mockito.when(step.execute(request != null ? request : Mockito.any())).thenReturn(response);
        Mockito.when(step.getIdentifier()).thenReturn(stepId);
        return step;
    }

    private StatelessProgressiveAuthenticator createObjectUnderTest(
            AuthenticationStep... authSteps) {
        return new StatelessProgressiveAuthenticator() {
            @Override
            public List<AuthenticationStep> authenticationSteps() {
                return Arrays.asList(authSteps);
            }
        };
    }
}
