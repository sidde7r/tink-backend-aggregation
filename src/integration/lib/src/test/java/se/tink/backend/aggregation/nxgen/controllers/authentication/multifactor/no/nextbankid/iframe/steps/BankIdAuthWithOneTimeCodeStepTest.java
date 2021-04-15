package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.HtmlLocators.LOC_ONE_TIME_CODE_INPUT;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.HtmlLocators.LOC_SUBMIT_BUTTON;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdTestUtils.verifyThatFromUsersPerspectiveThrowableIsTheSameAsGivenAgentException;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreen.ENTER_BANK_ID_PASSWORD_SCREEN;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreen.ENTER_SSN_SCREEN;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.List;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.bankidno.BankIdNOError;
import se.tink.backend.aggregation.agents.utils.supplementalfields.NorwegianFields;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.BankIdWebDriver;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreen;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreensManager;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreensQuery;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableKey;

@RunWith(JUnitParamsRunner.class)
public class BankIdAuthWithOneTimeCodeStepTest {

    private static final List<String> VALID_ONE_TIME_CODES = Arrays.asList("123456", "000000");
    private static final List<String> INVALID_ONE_TIME_CODES =
            Arrays.asList("1234567", "12345", "12345a", "123456.");

    /*
    Mocks
     */
    private BankIdWebDriver driver;
    private BankIdScreensManager screensManager;
    private Catalog catalog;
    private SupplementalInformationController supplementalInformationController;

    private InOrder mocksToVerifyInOrder;

    /*
    Real
     */
    private BankIdAuthWithOneTimeCodeStep authWithOneTimeCodeStep;

    @Before
    public void setup() {
        driver = mock(BankIdWebDriver.class);
        screensManager = mock(BankIdScreensManager.class);
        catalog = mock(Catalog.class);
        when(catalog.getString(any(LocalizableKey.class))).thenReturn("whatever");
        supplementalInformationController = mock(SupplementalInformationController.class);

        mocksToVerifyInOrder = inOrder(driver, screensManager, supplementalInformationController);

        authWithOneTimeCodeStep =
                new BankIdAuthWithOneTimeCodeStep(
                        driver, screensManager, catalog, supplementalInformationController);
    }

    @Test
    @Parameters(method = "validCodes")
    public void should_ask_user_to_provide_one_time_code_then_enter_it_and_wait_for_password_screen(
            String validCode) {
        // given
        Field expectedField = NorwegianFields.BankIdOneTimeCodeField.build(catalog);
        mockUserOneTimeCodeResponse(expectedField, validCode);

        mockScreenDetected(ENTER_BANK_ID_PASSWORD_SCREEN);

        // when
        authWithOneTimeCodeStep.authenticateWithOneTimeCode();

        // then
        mocksToVerifyInOrder
                .verify(supplementalInformationController)
                .askSupplementalInformationSync(expectedField);

        mocksToVerifyInOrder.verify(driver).setValueToElement(validCode, LOC_ONE_TIME_CODE_INPUT);
        mocksToVerifyInOrder.verify(driver).clickButton(LOC_SUBMIT_BUTTON);

        mocksToVerifyInOrder
                .verify(screensManager)
                .waitForAnyScreenFromQuery(
                        BankIdScreensQuery.builder()
                                .waitForScreens(ENTER_BANK_ID_PASSWORD_SCREEN, ENTER_SSN_SCREEN)
                                .waitForSeconds(10)
                                .verifyNoErrorScreens(true)
                                .build());

        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @SuppressWarnings("unused")
    private static Object[] validCodes() {
        return VALID_ONE_TIME_CODES.toArray();
    }

    @Test
    @Parameters(method = "validCodes")
    public void should_recognize_invalid_ssn_or_one_time_code_error_by_detecting_enter_ssn_screen(
            String validCode) {
        // given
        Field expectedField = NorwegianFields.BankIdOneTimeCodeField.build(catalog);
        mockUserOneTimeCodeResponse(expectedField, validCode);

        mockScreenDetected(ENTER_SSN_SCREEN);

        // when
        Throwable throwable =
                catchThrowable(() -> authWithOneTimeCodeStep.authenticateWithOneTimeCode());

        // then
        verifyThatFromUsersPerspectiveThrowableIsTheSameAsGivenAgentException(
                throwable, BankIdNOError.INVALID_SSN_OR_ONE_TIME_CODE.exception());

        mocksToVerifyInOrder
                .verify(supplementalInformationController)
                .askSupplementalInformationSync(expectedField);

        mocksToVerifyInOrder.verify(driver).setValueToElement(validCode, LOC_ONE_TIME_CODE_INPUT);
        mocksToVerifyInOrder.verify(driver).clickButton(LOC_SUBMIT_BUTTON);

        mocksToVerifyInOrder
                .verify(screensManager)
                .waitForAnyScreenFromQuery(
                        BankIdScreensQuery.builder()
                                .waitForScreens(ENTER_BANK_ID_PASSWORD_SCREEN, ENTER_SSN_SCREEN)
                                .waitForSeconds(10)
                                .verifyNoErrorScreens(true)
                                .build());

        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    @Parameters(method = "invalidCodes")
    public void should_throw_invalid_one_time_code_format_error(String invalidCode) {
        // given
        Field expectedField = NorwegianFields.BankIdOneTimeCodeField.build(catalog);
        mockUserOneTimeCodeResponse(expectedField, invalidCode);

        // when
        Throwable throwable =
                catchThrowable(() -> authWithOneTimeCodeStep.authenticateWithOneTimeCode());

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Incorrect format for one-time code: " + invalidCode);

        mocksToVerifyInOrder
                .verify(supplementalInformationController)
                .askSupplementalInformationSync(expectedField);
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @SuppressWarnings("unused")
    private static Object[] invalidCodes() {
        return INVALID_ONE_TIME_CODES.toArray();
    }

    private void mockUserOneTimeCodeResponse(Field field, String oneTimeCode) {
        when(supplementalInformationController.askSupplementalInformationSync(any()))
                .thenReturn(ImmutableMap.of(field.getName(), oneTimeCode));
    }

    private void mockScreenDetected(BankIdScreen screen) {
        when(screensManager.waitForAnyScreenFromQuery(any())).thenReturn(screen);
    }
}
