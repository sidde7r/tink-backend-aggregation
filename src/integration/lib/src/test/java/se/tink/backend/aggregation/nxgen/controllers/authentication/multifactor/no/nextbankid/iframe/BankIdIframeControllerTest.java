package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe;

import static org.apache.commons.lang3.StringUtils.repeat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdIframeFirstWindow.AUTHENTICATE_WITH_DEFAULT_2FA_METHOD;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdIframeFirstWindow.ENTER_SSN;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdTestUtils.verifyThatFromUsersPerspectiveThrowableIsTheSameAsGivenAgentException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.bankidno.BankIdNOError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.steps.BankIdEnterPasswordStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.steps.BankIdEnterSSNStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.steps.BankIdPerform2FAStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.steps.BankIdVerifyAuthenticationStep;

@RunWith(JUnitParamsRunner.class)
public class BankIdIframeControllerTest {

    private static final List<String> VALID_SSNs = Arrays.asList(repeat("1", 11), "12345678901");
    private static final List<String> INVALID_SSNs =
            Arrays.asList(null, "", repeat("1", 10), repeat("1", 12), repeat("1", 10) + "a");

    private static final List<String> VALID_PASSWORDS =
            Arrays.asList(repeat("1", 6), repeat("1", 255), "1432$%$^%&^%@");
    private static final List<String> INVALID_PASSWORDS =
            Arrays.asList(null, "", repeat("1", 5), repeat("1", 256));

    /*
    Mocks
     */
    private BankIdAuthenticationState authenticationState;
    private BankIdEnterSSNStep enterSSNStep;
    private BankIdPerform2FAStep perform2FAStep;
    private BankIdEnterPasswordStep enterPrivatePasswordStep;
    private BankIdVerifyAuthenticationStep verifyAuthenticationStep;

    private Credentials credentials;
    private InOrder mocksToVerifyInOrder;

    /*
    Real
     */
    private BankIdIframeController iframeController;

    @Before
    public void setup() {
        authenticationState = mock(BankIdAuthenticationState.class);
        enterSSNStep = mock(BankIdEnterSSNStep.class);
        perform2FAStep = mock(BankIdPerform2FAStep.class);
        enterPrivatePasswordStep = mock(BankIdEnterPasswordStep.class);
        verifyAuthenticationStep = mock(BankIdVerifyAuthenticationStep.class);

        credentials = mock(Credentials.class);
        mocksToVerifyInOrder =
                inOrder(
                        credentials,
                        enterSSNStep,
                        perform2FAStep,
                        enterPrivatePasswordStep,
                        verifyAuthenticationStep);

        iframeController =
                new BankIdIframeController(
                        authenticationState,
                        enterSSNStep,
                        perform2FAStep,
                        enterPrivatePasswordStep,
                        verifyAuthenticationStep);
    }

    @Test
    @Parameters(method = "validCredentialsParams")
    public void should_execute_steps_in_correct_order_starting_from_enter_ssn(
            String ssn, String password) {
        // given
        when(credentials.getField(Field.Key.USERNAME)).thenReturn(ssn);
        when(credentials.getField(Field.Key.BANKID_PASSWORD)).thenReturn(password);
        when(authenticationState.getFirstIframeWindow()).thenReturn(ENTER_SSN);

        // when
        iframeController.authenticateWithCredentials(credentials);

        // then
        mocksToVerifyInOrder.verify(credentials).getField(Field.Key.USERNAME);
        mocksToVerifyInOrder.verify(credentials).getField(Field.Key.BANKID_PASSWORD);
        mocksToVerifyInOrder.verify(enterSSNStep).enterSSN(ssn);
        mocksToVerifyInOrder.verify(perform2FAStep).perform2FA();
        mocksToVerifyInOrder.verify(enterPrivatePasswordStep).enterPrivatePassword(password);
        mocksToVerifyInOrder.verify(verifyAuthenticationStep).verify();

        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @SuppressWarnings("unused")
    private static Object[] validCredentialsParams() {
        return getPairWiseCombinations(VALID_SSNs, VALID_PASSWORDS).toArray();
    }

    @Test
    @Parameters(method = "validCredentialsParams")
    public void should_execute_steps_in_correct_order_starting_from_2FA(
            String ssn, String password) {
        // given
        when(credentials.getField(Field.Key.USERNAME)).thenReturn(ssn);
        when(credentials.getField(Field.Key.BANKID_PASSWORD)).thenReturn(password);
        when(authenticationState.getFirstIframeWindow())
                .thenReturn(AUTHENTICATE_WITH_DEFAULT_2FA_METHOD);

        // when
        iframeController.authenticateWithCredentials(credentials);

        // then
        mocksToVerifyInOrder.verify(credentials).getField(Field.Key.USERNAME);
        mocksToVerifyInOrder.verify(credentials).getField(Field.Key.BANKID_PASSWORD);
        mocksToVerifyInOrder.verify(perform2FAStep).perform2FA();
        mocksToVerifyInOrder.verify(enterPrivatePasswordStep).enterPrivatePassword(password);
        mocksToVerifyInOrder.verify(verifyAuthenticationStep).verify();

        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    @Parameters(method = "invalidSsnParams")
    public void should_detect_invalid_ssn(String invalidSsn, String validPassword) {
        // given
        when(credentials.getField(Field.Key.USERNAME)).thenReturn(invalidSsn);
        when(credentials.getField(Field.Key.BANKID_PASSWORD)).thenReturn(validPassword);
        when(authenticationState.getFirstIframeWindow())
                .thenReturn(AUTHENTICATE_WITH_DEFAULT_2FA_METHOD);

        // when
        Throwable throwable =
                catchThrowable(() -> iframeController.authenticateWithCredentials(credentials));

        // then
        verifyThatFromUsersPerspectiveThrowableIsTheSameAsGivenAgentException(
                throwable, BankIdNOError.INVALID_SSN_FORMAT.exception());

        mocksToVerifyInOrder.verify(credentials).getField(Field.Key.USERNAME);
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @SuppressWarnings("unused")
    private static Object[] invalidSsnParams() {
        return getPairWiseCombinations(INVALID_SSNs, VALID_PASSWORDS).toArray();
    }

    @Test
    @Parameters(method = "invalidPasswordParams")
    public void should_detect_invalid_password(String validSsn, String invalidPassword) {
        // given
        when(credentials.getField(Field.Key.USERNAME)).thenReturn(validSsn);
        when(credentials.getField(Field.Key.BANKID_PASSWORD)).thenReturn(invalidPassword);
        when(authenticationState.getFirstIframeWindow())
                .thenReturn(AUTHENTICATE_WITH_DEFAULT_2FA_METHOD);

        // when
        Throwable throwable =
                catchThrowable(() -> iframeController.authenticateWithCredentials(credentials));

        // then
        verifyThatFromUsersPerspectiveThrowableIsTheSameAsGivenAgentException(
                throwable, BankIdNOError.INVALID_BANK_ID_PASSWORD_FORMAT.exception());

        mocksToVerifyInOrder.verify(credentials).getField(Field.Key.USERNAME);
        mocksToVerifyInOrder.verify(credentials).getField(Field.Key.BANKID_PASSWORD);
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @SuppressWarnings("unused")
    private static Object[] invalidPasswordParams() {
        return getPairWiseCombinations(VALID_SSNs, INVALID_PASSWORDS).toArray();
    }

    private static <T> List<Object[]> getPairWiseCombinations(List<T> list1, List<T> list2) {
        List<Object[]> result = new ArrayList<>();

        for (T item1 : list1) {
            for (T item2 : list2) {
                result.add(new Object[] {item1, item2});
            }
        }

        return result;
    }
}
