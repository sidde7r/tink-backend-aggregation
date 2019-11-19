package se.tink.backend.aggregation.nxgen.controllers.authentication;

import static org.junit.Assert.*;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;

public class StatelessProgressiveAuthenticatorTest {

    @Test
    public void
            processAuthenticationShouldRespondWithFinalResponseWhenContainsOnlyOneAutomaticStep()
                    throws AuthenticationException, AuthorizationException {
        // given
        SteppableAuthenticationRequest request = SteppableAuthenticationRequest.initialRequest();
        AuthenticationStep automaticStep = Mockito.mock(AuthenticationStep.class);
        Mockito.when(
                        automaticStep.execute(
                                Mockito.eq(request.getPayload()),
                                Mockito.any(PersistentObject.class)))
                .thenReturn(Optional.empty());
        Mockito.when(automaticStep.getIdentifier()).thenReturn("stepId");
        Iterable<AuthenticationStep> steps = Lists.newArrayList(automaticStep);
        StatelessProgressiveAuthenticator<PersistentObject> objectUnderTest =
                createAuthenticator(steps);
        // when
        SteppableAuthenticationResponse result = objectUnderTest.processAuthentication(request);
        // then
        Assert.assertNotNull(result.getPersistentData());
        Assert.assertFalse(result.getStepIdentifier().isPresent());
        Assert.assertNull(result.getSupplementInformationRequester());
    }

    @Test
    public void
            processAuthenticationShouldRespondWithIntermediateResponseWhenContainsOnlyOneManualStep()
                    throws AuthenticationException, AuthorizationException {
        // given
        final String stepId = "stepId";
        SteppableAuthenticationRequest firstRequest =
                SteppableAuthenticationRequest.initialRequest();

        AuthenticationStep manualStep = Mockito.mock(AuthenticationStep.class);
        SupplementInformationRequester supplementInformationRequester =
                SupplementInformationRequester.empty();
        Mockito.when(
                        manualStep.execute(
                                Mockito.eq(firstRequest.getPayload()),
                                Mockito.any(PersistentObject.class)))
                .thenReturn(Optional.of(supplementInformationRequester));
        Mockito.when(manualStep.getIdentifier()).thenReturn(stepId);
        Iterable<AuthenticationStep> steps = Lists.newArrayList(manualStep);
        StatelessProgressiveAuthenticator<PersistentObject> objectUnderTest =
                createAuthenticator(steps);
        // when
        SteppableAuthenticationResponse result =
                objectUnderTest.processAuthentication(firstRequest);
        // then
        Assert.assertNotNull(result.getPersistentData());
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
                        AuthenticationRequest.fromCallbackData(ImmutableMap.of("key", "value")),
                        "{}");
        AuthenticationStep stepToOmit = Mockito.mock(AuthenticationStep.class);
        Mockito.when(stepToOmit.execute(Mockito.any(), Mockito.any(PersistentObject.class)))
                .thenReturn(Optional.of(SupplementInformationRequester.empty()));
        Mockito.when(stepToOmit.getIdentifier()).thenReturn("stepToOmit");
        AuthenticationStep stepToExecute = Mockito.mock(AuthenticationStep.class);
        Mockito.when(stepToExecute.getIdentifier()).thenReturn(stepIdToExecute);
        Mockito.when(
                        stepToExecute.execute(
                                Mockito.eq(request.getPayload()),
                                Mockito.any(PersistentObject.class)))
                .thenReturn(Optional.empty());
        Iterable<AuthenticationStep> steps = Lists.newArrayList(stepToOmit, stepToExecute);
        StatelessProgressiveAuthenticator<PersistentObject> objectUnderTest =
                createAuthenticator(steps);
        // when
        SteppableAuthenticationResponse result = objectUnderTest.processAuthentication(request);
        // then
        Assert.assertNotNull(result.getPersistentData());
        Assert.assertFalse(result.getStepIdentifier().isPresent());
        Assert.assertNull(result.getSupplementInformationRequester());
    }

    @Test(expected = IllegalStateException.class)
    public void processAuthenticationShouldThrowExceptionWhenStepDoesNotExist()
            throws AuthenticationException, AuthorizationException {
        // given
        SteppableAuthenticationRequest request =
                SteppableAuthenticationRequest.subsequentRequest(
                        "wrongStepId",
                        AuthenticationRequest.fromCallbackData(ImmutableMap.of("key", "value")),
                        "{}");
        AuthenticationStep step = Mockito.mock(AuthenticationStep.class);
        Mockito.when(step.getIdentifier()).thenReturn("stepId");

        Iterable<AuthenticationStep> steps = Lists.newArrayList(step);
        StatelessProgressiveAuthenticator<PersistentObject> objectUnderTest =
                createAuthenticator(steps);
        // when
        objectUnderTest.processAuthentication(request);
        // then
        // IllegalStateException
    }

    private StatelessProgressiveAuthenticator createAuthenticator(
            Iterable<? extends AuthenticationStep> authSteps) {
        return new StatelessProgressiveAuthenticator(PersistentObject.class) {
            @Override
            public Iterable<? extends AuthenticationStep> authenticationSteps()
                    throws AuthenticationException, AuthorizationException {
                return authSteps;
            }
        };
    }

    public static class PersistentObject {
        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
