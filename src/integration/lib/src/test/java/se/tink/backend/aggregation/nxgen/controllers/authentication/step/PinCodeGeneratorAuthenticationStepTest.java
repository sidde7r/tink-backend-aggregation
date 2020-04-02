package se.tink.backend.aggregation.nxgen.controllers.authentication.step;

import java.util.regex.Pattern;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;

public class PinCodeGeneratorAuthenticationStepTest {

    private PinCodeGeneratorAuthenticationStep objectUnderStep;
    private CallbackProcessorSingleData callbackProcessor;

    @Before
    public void init() {
        callbackProcessor = Mockito.mock(CallbackProcessorSingleData.class);
        objectUnderStep = new PinCodeGeneratorAuthenticationStep(callbackProcessor);
    }

    @Test
    public void shouldGeneratePinCodeWithDefault4DigitsLength()
            throws AuthenticationException, AuthorizationException {
        // given
        AuthenticationRequest authenticationRequest =
                new AuthenticationRequest(Mockito.mock(Credentials.class));
        Pattern pattern = Pattern.compile("^[0-9]{4}$");
        // when
        objectUnderStep.execute(authenticationRequest);
        // then
        Mockito.verify(callbackProcessor)
                .process(Mockito.argThat(pin -> pattern.matcher(pin).find()));
    }

    @Test
    public void shouldGeneratePinCodeWith8DigitsLength()
            throws AuthenticationException, AuthorizationException {
        // given
        objectUnderStep = new PinCodeGeneratorAuthenticationStep(callbackProcessor, 8);
        AuthenticationRequest authenticationRequest =
                new AuthenticationRequest(Mockito.mock(Credentials.class));
        Pattern pattern = Pattern.compile("^[0-9]{8}$");
        // when
        objectUnderStep.execute(authenticationRequest);
        // then
        Mockito.verify(callbackProcessor)
                .process(Mockito.argThat(pin -> pattern.matcher(pin).find()));
    }
}
