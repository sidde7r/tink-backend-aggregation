package se.tink.backend.aggregation.nxgen.controllers.authentication.step;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;

public class CredentialsAuthenticationStepTest {

    private CredentialsAuthenticationStep step;

    private CredentialsAuthenticationStep.CallbackProcessor callback;

    @Before
    public void setUp() {
        callback = mock(CredentialsAuthenticationStep.CallbackProcessor.class);
        step = new CredentialsAuthenticationStep(callback);
    }

    @Test
    public void executeShouldInvokeCallbackWithCredentials()
            throws AuthenticationException, AuthorizationException {
        // given
        Credentials credentials = new Credentials();
        // and
        AuthenticationRequest request = new AuthenticationRequest(credentials);

        // when
        step.execute(request);

        // then
        verify(callback).process(credentials);
    }

    @Test
    public void executeShouldReturnFinishedResponseStep()
            throws AuthenticationException, AuthorizationException {
        // given
        Credentials credentials = new Credentials();
        // and
        AuthenticationRequest request = new AuthenticationRequest(credentials);

        // when
        AuthenticationStepResponse result = step.execute(request);

        // then
        assertThat(result.getNextStepId()).isNotPresent();
    }
}
