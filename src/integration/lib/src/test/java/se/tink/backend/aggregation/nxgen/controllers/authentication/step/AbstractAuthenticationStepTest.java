package se.tink.backend.aggregation.nxgen.controllers.authentication.step;

import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;

public class AbstractAuthenticationStepTest {

    @Test
    public void getStepIdentifierShouldReturnCustomIdentifier() {
        // given
        final String customStepId = "customStepId";
        AbstractAuthenticationStep objectUnderTest = new TestAuthenticationStep(customStepId);
        // when
        String result = objectUnderTest.getIdentifier();
        // then
        Assert.assertEquals(customStepId, result);
    }

    @Test
    public void getStepIdentifierShouldReturnDefaultIdentifier() {
        // given
        AbstractAuthenticationStep objectUnderTest = new TestAuthenticationStep();
        // when
        String result = objectUnderTest.getIdentifier();
        // then
        Assert.assertEquals(TestAuthenticationStep.class.getName(), result);
    }

    private class TestAuthenticationStep extends AbstractAuthenticationStep {

        public TestAuthenticationStep(String stepId) {
            super(stepId);
        }

        public TestAuthenticationStep() {}

        @Override
        public AuthenticationStepResponse execute(AuthenticationRequest request)
                throws AuthenticationException, AuthorizationException {
            return AuthenticationStepResponse.executeNextStep();
        }
    }
}
