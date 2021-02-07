package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.codeapp;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import se.tink.backend.agents.rpc.Credentials;

public class NemIdAuthorizeWithCodeAppStepTest {

    private NemIdCodeAppAskUserToApproveRequestStep waitForUserToApproveRequestStep;
    private NemIdCodeAppCollectTokenStep collectTokenStep;

    private NemIdAuthorizeWithCodeAppStep authorizeWithCodeAppStep;

    private InOrder mocksToVerifyInOrder;

    @Before
    public void setUp() {
        waitForUserToApproveRequestStep = mock(NemIdCodeAppAskUserToApproveRequestStep.class);
        collectTokenStep = mock(NemIdCodeAppCollectTokenStep.class);
        mocksToVerifyInOrder = inOrder(waitForUserToApproveRequestStep, collectTokenStep);

        authorizeWithCodeAppStep =
                new NemIdAuthorizeWithCodeAppStep(
                        waitForUserToApproveRequestStep, collectTokenStep);
    }

    @Test
    public void should_run_steps_in_correct_order() {
        // given
        Credentials credentials = mock(Credentials.class);

        // when
        authorizeWithCodeAppStep.getNemIdTokenWithCodeAppAuth(credentials);

        // then
        mocksToVerifyInOrder
                .verify(waitForUserToApproveRequestStep)
                .sendCodeAppRequestAndWaitForResponse(credentials);
        mocksToVerifyInOrder.verify(collectTokenStep).collectToken();
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }
}
