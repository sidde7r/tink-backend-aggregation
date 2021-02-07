package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.codecard;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import se.tink.backend.agents.rpc.Credentials;

public class NemIdAuthorizeWithCodeCardStepTest {

    private NemIdCodeCardAskUserForCodeStep askUserForCodeStep;
    private NemIdCodeCardGetTokenStep getTokenStep;

    private NemIdAuthorizeWithCodeCardStep authorizeWithCodeCardStep;

    private InOrder mocksToVerifyInOrder;

    @Before
    public void setUp() {
        askUserForCodeStep = mock(NemIdCodeCardAskUserForCodeStep.class);
        getTokenStep = mock(NemIdCodeCardGetTokenStep.class);

        authorizeWithCodeCardStep =
                new NemIdAuthorizeWithCodeCardStep(askUserForCodeStep, getTokenStep);

        mocksToVerifyInOrder = inOrder(askUserForCodeStep, getTokenStep);
    }

    @Test
    public void should_execute_steps_in_correct_order() {
        // given
        Credentials credentials = mock(Credentials.class);

        when(askUserForCodeStep.askForCodeAndValidateResponse(any()))
                .thenReturn("SAMPLE CODE CARD CODE");

        // when
        authorizeWithCodeCardStep.getNemIdTokenWithCodeCardAuth(credentials);

        // then
        mocksToVerifyInOrder.verify(askUserForCodeStep).askForCodeAndValidateResponse(credentials);
        mocksToVerifyInOrder.verify(getTokenStep).enterCodeAndGetToken("SAMPLE CODE CARD CODE");
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }
}
