package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdIframeFirstStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.steps.BankIdEnterPasswordStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.steps.BankIdEnterSSNStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.steps.BankIdPerform2FAStep;

public class BankIdIframeControllerTest {

    /*
    Mocks
     */
    private BankIdEnterSSNStep enterSSNStep;
    private BankIdPerform2FAStep perform2FAStep;
    private BankIdEnterPasswordStep enterPrivatePasswordStep;

    private Credentials credentials;
    private InOrder mocksToVerifyInOrder;

    /*
    Real
     */
    private BankIdIframeController iframeController;

    @Before
    public void setup() {
        enterSSNStep = mock(BankIdEnterSSNStep.class);
        perform2FAStep = mock(BankIdPerform2FAStep.class);
        enterPrivatePasswordStep = mock(BankIdEnterPasswordStep.class);

        credentials = mock(Credentials.class);
        mocksToVerifyInOrder = inOrder(enterSSNStep, perform2FAStep, enterPrivatePasswordStep);

        iframeController =
                new BankIdIframeController(enterSSNStep, perform2FAStep, enterPrivatePasswordStep);
    }

    @Test
    public void should_execute_steps_in_correct_order_starting_from_enter_ssn() {
        // when
        iframeController.authenticateWithCredentials(credentials, BankIdIframeFirstStep.ENTER_SSN);

        // then
        mocksToVerifyInOrder.verify(enterSSNStep).enterSSN(credentials);
        mocksToVerifyInOrder.verify(perform2FAStep).perform2FA();
        mocksToVerifyInOrder.verify(enterPrivatePasswordStep).enterPrivatePassword(credentials);

        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void should_execute_steps_in_correct_order_starting_from_2FA() {
        // when
        iframeController.authenticateWithCredentials(
                credentials, BankIdIframeFirstStep.AUTHENTICATE_WITH_DEFAULT_2FA_METHOD);

        // then
        mocksToVerifyInOrder.verify(perform2FAStep).perform2FA();
        mocksToVerifyInOrder.verify(enterPrivatePasswordStep).enterPrivatePassword(credentials);

        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }
}
