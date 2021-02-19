package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemId2FAMethod;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.codeapp.NemIdAuthorizeWithCodeAppStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.codecard.NemIdAuthorizeWithCodeCardStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.codetoken.NemIdAuthorizeWithCodeTokenStep;

public class NemIdPerform2FAStepTest {

    private NemIdAuthorizeWithCodeAppStep authorizeWithCodeAppStep;
    private NemIdAuthorizeWithCodeCardStep authorizeWithCodeCardStep;
    private NemIdAuthorizeWithCodeTokenStep authorizeWithCodeTokenStep;
    private InOrder mocksToVerifyInOrder;

    private Credentials credentials;

    private NemIdPerform2FAStep perform2FAStep;

    @Before
    public void setup() {
        authorizeWithCodeAppStep = mock(NemIdAuthorizeWithCodeAppStep.class);
        authorizeWithCodeCardStep = mock(NemIdAuthorizeWithCodeCardStep.class);
        authorizeWithCodeTokenStep = mock(NemIdAuthorizeWithCodeTokenStep.class);
        mocksToVerifyInOrder =
                inOrder(
                        authorizeWithCodeAppStep,
                        authorizeWithCodeCardStep,
                        authorizeWithCodeTokenStep);

        credentials = mock(Credentials.class);

        perform2FAStep =
                new NemIdPerform2FAStep(
                        authorizeWithCodeAppStep,
                        authorizeWithCodeCardStep,
                        authorizeWithCodeTokenStep);
    }

    @Test
    public void should_authenticate_with_code_app() {
        // when
        perform2FAStep.authenticateToGetNemIdToken(NemId2FAMethod.CODE_APP, credentials);

        // then
        mocksToVerifyInOrder
                .verify(authorizeWithCodeAppStep)
                .getNemIdTokenWithCodeAppAuth(credentials);
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void should_authenticate_with_code_card() {
        // when
        perform2FAStep.authenticateToGetNemIdToken(NemId2FAMethod.CODE_CARD, credentials);

        // then
        mocksToVerifyInOrder
                .verify(authorizeWithCodeCardStep)
                .getNemIdTokenWithCodeCardAuth(credentials);
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void should_authenticate_with_code_token() {
        // when
        perform2FAStep.authenticateToGetNemIdToken(NemId2FAMethod.CODE_TOKEN, credentials);

        // then
        mocksToVerifyInOrder
                .verify(authorizeWithCodeTokenStep)
                .getNemIdTokenWithCodeTokenAuth(credentials);
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }
}
