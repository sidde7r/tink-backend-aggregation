package se.tink.backend.aggregation.nxgen.controllers.authentication;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class StatelessProgressiveAuthenticatorTest {

    @Test
    public void
            processAuthenticationShouldRespondWithFinalResponseWhenContainsOnlyOneAutomaticStep()
                    throws AuthenticationException, AuthorizationException {
        // given
        SteppableAuthenticationRequest request =
                SteppableAuthenticationRequest.initialRequest(Mockito.mock(Credentials.class));
        AuthenticationStep automaticStep =
                mockAuthenticationStep("stepId", request.getPayload(), null);
        Iterable<AuthenticationStep> steps = Lists.newArrayList(automaticStep);
        StatelessProgressiveAuthenticator objectUnderTest = createAuthenticator(steps);
        // when
        SteppableAuthenticationResponse result = objectUnderTest.processAuthentication(request);
        // then
        Assert.assertFalse(result.getStepIdentifier().isPresent());
        Assert.assertNull(result.getSupplementInformationRequester());
    }

    private AuthenticationStep mockAuthenticationStep(
            String stepId, AuthenticationRequest request, SupplementInformationRequester response)
            throws AuthenticationException, AuthorizationException {
        AuthenticationStep step = Mockito.mock(AuthenticationStep.class);
        Mockito.when(step.execute(request)).thenReturn(Optional.ofNullable(response));
        Mockito.when(step.getIdentifier()).thenReturn(stepId);
        return step;
    }

    @Test
    public void
            processAuthenticationShouldRespondWithIntermediateResponseWhenContainsOnlyOneManualStep()
                    throws AuthenticationException, AuthorizationException {
        // given
        final String stepId = "stepId";
        SteppableAuthenticationRequest firstRequest =
                SteppableAuthenticationRequest.initialRequest(Mockito.mock(Credentials.class));

        SupplementInformationRequester supplementInformationRequester =
                SupplementInformationRequester.empty();
        AuthenticationStep manualStep =
                mockAuthenticationStep(
                        stepId, firstRequest.getPayload(), supplementInformationRequester);
        Iterable<AuthenticationStep> steps = Lists.newArrayList(manualStep);
        StatelessProgressiveAuthenticator objectUnderTest = createAuthenticator(steps);
        // when
        SteppableAuthenticationResponse result =
                objectUnderTest.processAuthentication(firstRequest);
        // then
        Assert.assertTrue(result.getStepIdentifier().isPresent());
        Assert.assertEquals(stepId, result.getStepIdentifier().get());
        Assert.assertNotNull(result.getSupplementInformationRequester());
    }

    @Test
    public void processAuthenticationShouldExecuteStepWithRequestedIdentifier()
            throws AuthenticationException, AuthorizationException {
        // given
        final String stepIdToExecute = "stepIdToExecute";
        SteppableAuthenticationRequest request =
                SteppableAuthenticationRequest.subsequentRequest(
                        stepIdToExecute,
                        new AuthenticationRequest(Mockito.mock(Credentials.class))
                                .withCallbackData(ImmutableMap.of("key", "value")));
        AuthenticationStep stepToOmit = Mockito.mock(AuthenticationStep.class);
        Mockito.when(stepToOmit.execute(Mockito.any()))
                .thenReturn(Optional.of(SupplementInformationRequester.empty()));
        Mockito.when(stepToOmit.getIdentifier()).thenReturn("stepToOmit");
        AuthenticationStep stepToExecute =
                mockAuthenticationStep(stepIdToExecute, request.getPayload(), null);
        Iterable<AuthenticationStep> steps = Lists.newArrayList(stepToOmit, stepToExecute);
        StatelessProgressiveAuthenticator objectUnderTest = createAuthenticator(steps);
        // when
        SteppableAuthenticationResponse result = objectUnderTest.processAuthentication(request);
        // then
        Assert.assertFalse(result.getStepIdentifier().isPresent());
        Assert.assertNull(result.getSupplementInformationRequester());
        Assert.assertTrue(request.getPayload().getUserInputs().isEmpty());
        Assert.assertTrue(request.getPayload().getCallbackData().isEmpty());
    }

    @Test(expected = IllegalStateException.class)
    public void processAuthenticationShouldThrowExceptionWhenStepDoesNotExist()
            throws AuthenticationException, AuthorizationException {
        // given
        SteppableAuthenticationRequest request =
                SteppableAuthenticationRequest.subsequentRequest(
                        "wrongStepId",
                        new AuthenticationRequest(Mockito.mock(Credentials.class))
                                .withCallbackData(ImmutableMap.of("key", "value")));
        AuthenticationStep step = Mockito.mock(AuthenticationStep.class);
        Mockito.when(step.getIdentifier()).thenReturn("stepId");

        Iterable<AuthenticationStep> steps = Lists.newArrayList(step);
        StatelessProgressiveAuthenticator objectUnderTest = createAuthenticator(steps);
        // when
        objectUnderTest.processAuthentication(request);
        // then
        // IllegalStateException
    }

    private StatelessProgressiveAuthenticator createAuthenticator(
            Iterable<? extends AuthenticationStep> authSteps) {
        return new StatelessProgressiveAuthenticator() {
            @Override
            public boolean isManualAuthentication(CredentialsRequest request) {
                return true;
            }

            @Override
            public Iterable<? extends AuthenticationStep> authenticationSteps()
                    throws AuthenticationException, AuthorizationException {
                return authSteps;
            }
        };
    }
}
