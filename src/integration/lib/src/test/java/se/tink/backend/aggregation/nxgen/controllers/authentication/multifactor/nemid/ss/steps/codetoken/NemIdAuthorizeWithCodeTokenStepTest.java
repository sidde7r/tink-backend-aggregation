package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.codetoken;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import se.tink.backend.agents.rpc.Credentials;

public class NemIdAuthorizeWithCodeTokenStepTest {

    private NemIdCodeTokenAskUserForCodeStep askUserForCodeStep;
    private NemIdCodeTokenGetTokenStep getTokenStep;

    private NemIdAuthorizeWithCodeTokenStep authorizeWithCodeTokenStep;

    private InOrder mocksToVerifyInOrder;

    @Before
    public void setUp() {
        askUserForCodeStep = mock(NemIdCodeTokenAskUserForCodeStep.class);
        getTokenStep = mock(NemIdCodeTokenGetTokenStep.class);

        authorizeWithCodeTokenStep =
                new NemIdAuthorizeWithCodeTokenStep(askUserForCodeStep, getTokenStep);

        mocksToVerifyInOrder = inOrder(askUserForCodeStep, getTokenStep);
    }

    @Test
    public void should_execute_steps_in_correct_order() {
        // given
        Credentials credentials = mock(Credentials.class);

        when(askUserForCodeStep.askForCodeAndValidateResponse(any()))
                .thenReturn("SAMPLE CODE TOKEN CODE");

        // when
        authorizeWithCodeTokenStep.getNemIdTokenWithCodeTokenAuth(credentials);

        // then
        mocksToVerifyInOrder.verify(askUserForCodeStep).askForCodeAndValidateResponse(credentials);
        mocksToVerifyInOrder.verify(getTokenStep).enterCodeAndGetToken("SAMPLE CODE TOKEN CODE");
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }
}
