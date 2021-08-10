package se.tink.backend.aggregation.agents.nxgen.no.banks.dnbbankid.authenticator;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.no.banks.dnbbankid.DnbConstants.HtmlLocators.LOC_CLOSE_COOKIES_POPUP_BUTTON;
import static se.tink.backend.aggregation.agents.nxgen.no.banks.dnbbankid.DnbConstants.HtmlLocators.LOC_ERROR_MESSAGE;
import static se.tink.backend.aggregation.agents.nxgen.no.banks.dnbbankid.DnbConstants.HtmlLocators.LOC_SSN_INPUT;
import static se.tink.backend.aggregation.agents.nxgen.no.banks.dnbbankid.DnbConstants.HtmlLocators.LOC_SUBMIT_BUTTON;
import static se.tink.backend.aggregation.agents.nxgen.no.banks.dnbbankid.DnbConstants.Messages.INVALID_SSN_ERROR_MESSAGE;
import static se.tink.backend.aggregation.agents.nxgen.no.banks.dnbbankid.DnbConstants.Url.INIT_LOGIN;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.HtmlLocators.LOC_IFRAME;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdTestUtils.mockLocatorDoesNotExists;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdTestUtils.mockLocatorExists;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdTestUtils.mockWebElement;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdTestUtils.mockWebElementWithText;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdTestUtils.verifyThrowableIsTheSameAsGivenAgentException;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.openqa.selenium.WebElement;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentException;
import se.tink.backend.aggregation.agents.exceptions.bankidno.BankIdNOError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdIframeFirstWindow;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.BankIdWebDriver;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.BankIdElementsSearchQuery;

@RunWith(JUnitParamsRunner.class)
public class DnbBankIdIFrameInitializerTest {

    private static final String SAMPLE_SSN = "SAMPLE_SSN";

    /*
    Mocks
     */
    private BankIdWebDriver driver;
    private InOrder mocksToVerifyInOrder;

    /*
    Real
     */
    private DnbBankIdIframeInitializer iFrameInitializer;

    @Before
    public void setup() {
        driver = mock(BankIdWebDriver.class);
        Credentials credentials = mock(Credentials.class);
        when(credentials.getField(Field.Key.USERNAME)).thenReturn(SAMPLE_SSN);

        mocksToVerifyInOrder = inOrder(driver, credentials);

        iFrameInitializer = new DnbBankIdIframeInitializer(credentials);
    }

    @Test
    public void should_initialize_bank_id_iframe() {
        // given
        mockLocatorExists(LOC_SSN_INPUT, driver);

        WebElement closeCookiesButton = mockWebElement();
        mockLocatorExists(LOC_CLOSE_COOKIES_POPUP_BUTTON, closeCookiesButton, driver);

        mockLocatorExists(LOC_IFRAME, driver);

        // when
        BankIdIframeFirstWindow firstWindowForIframe = iFrameInitializer.initializeIframe(driver);

        // then
        assertThat(firstWindowForIframe)
                .isEqualTo(BankIdIframeFirstWindow.AUTHENTICATE_WITH_DEFAULT_2FA_METHOD);

        verifyOpensWebsite();
        verifyWaitsForSSNInput();

        verifyWaitsForCookiesButton();
        verify(closeCookiesButton).click();
        verifyNoMoreInteractions(closeCookiesButton);

        verifySetsSSNInput();
        verifyClicksSubmit();
        verifyLooksForIframeOrErrors();
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void should_initialize_bank_id_iframe_without_closing_cookies_popup() {
        // given
        mockLocatorExists(LOC_SSN_INPUT, driver);
        mockLocatorDoesNotExists(LOC_CLOSE_COOKIES_POPUP_BUTTON, driver);
        mockLocatorExists(LOC_IFRAME, driver);

        // when
        BankIdIframeFirstWindow firstWindowForIframe = iFrameInitializer.initializeIframe(driver);

        // then
        assertThat(firstWindowForIframe)
                .isEqualTo(BankIdIframeFirstWindow.AUTHENTICATE_WITH_DEFAULT_2FA_METHOD);

        verifyOpensWebsite();
        verifyWaitsForSSNInput();
        verifyWaitsForCookiesButton();
        verifySetsSSNInput();
        verifyClicksSubmit();
        verifyLooksForIframeOrErrors();
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void should_throw_when_there_is_no_ssn_input() {
        // given
        mockLocatorDoesNotExists(LOC_SSN_INPUT, driver);

        // when
        Throwable throwable = catchThrowable(() -> iFrameInitializer.initializeIframe(driver));

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("[DNB] SSN input field not found");

        verifyOpensWebsite();
        verifyWaitsForSSNInput();
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    @Parameters(method = "errorMessageTestParams")
    public void should_throw_correct_exception_when_there_is_an_error_message(
            String message, AgentException expectedException) {
        // given
        mockLocatorExists(LOC_SSN_INPUT, driver);
        mockLocatorDoesNotExists(LOC_CLOSE_COOKIES_POPUP_BUTTON, driver);

        WebElement errorElement = mockWebElementWithText(message);
        mockLocatorExists(LOC_ERROR_MESSAGE, errorElement, driver);

        // when
        Throwable throwable = catchThrowable(() -> iFrameInitializer.initializeIframe(driver));

        // then
        verifyThrowableIsTheSameAsGivenAgentException(throwable, expectedException);

        verifyOpensWebsite();
        verifyWaitsForSSNInput();
        verifyWaitsForCookiesButton();
        verifySetsSSNInput();
        verifyClicksSubmit();
        verifyLooksForIframeOrErrors();
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @SuppressWarnings("unused")
    private Object[] errorMessageTestParams() {
        Stream<ErrorMessageTestParams> knownErrors =
                Stream.of(
                                ErrorMessageTestParams.builder()
                                        .message(INVALID_SSN_ERROR_MESSAGE)
                                        .expectedException(BankIdNOError.INVALID_SSN.exception())
                                        .build())
                        .map(
                                params ->
                                        asList(
                                                params.modifyMessage(String::toLowerCase),
                                                params.modifyMessage(String::toUpperCase),
                                                params.modifyMessage(
                                                        msg ->
                                                                "some_prefix"
                                                                        + msg
                                                                        + "some_suffix")))
                        .flatMap(List::stream);

        Stream<ErrorMessageTestParams> unknownErrors =
                Stream.of(
                        ErrorMessageTestParams.builder()
                                .message("!!@#%^")
                                .expectedException(
                                        BankIdNOError.UNKNOWN_BANK_ID_ERROR.exception(
                                                "[DNB] Unknown BankID iframe initialization error: !!@#%^"))
                                .build(),
                        ErrorMessageTestParams.builder()
                                .message("")
                                .expectedException(
                                        BankIdNOError.UNKNOWN_BANK_ID_ERROR.exception(
                                                "[DNB] Unknown BankID iframe initialization error: "))
                                .build(),
                        ErrorMessageTestParams.builder()
                                .message(null)
                                .expectedException(
                                        BankIdNOError.UNKNOWN_BANK_ID_ERROR.exception(
                                                "[DNB] Unknown BankID iframe initialization error: null"))
                                .build());

        return Stream.concat(knownErrors, unknownErrors)
                .map(ErrorMessageTestParams::toMethodParams)
                .toArray();
    }

    @Builder
    @RequiredArgsConstructor
    private static class ErrorMessageTestParams {

        private final String message;
        private final AgentException expectedException;

        private ErrorMessageTestParams modifyMessage(Function<String, String> modifier) {
            String newMessage = modifier.apply(message);
            return new ErrorMessageTestParams(newMessage, expectedException);
        }

        private Object[] toMethodParams() {
            return new Object[] {message, expectedException};
        }
    }

    private void verifyOpensWebsite() {
        mocksToVerifyInOrder.verify(driver).getUrl(INIT_LOGIN);
    }

    private void verifyWaitsForSSNInput() {
        mocksToVerifyInOrder
                .verify(driver)
                .searchForFirstMatchingLocator(
                        BankIdElementsSearchQuery.builder().searchFor(LOC_SSN_INPUT).build());
    }

    private void verifyWaitsForCookiesButton() {
        mocksToVerifyInOrder
                .verify(driver)
                .searchForFirstMatchingLocator(
                        BankIdElementsSearchQuery.builder()
                                .searchFor(LOC_CLOSE_COOKIES_POPUP_BUTTON)
                                .searchForSeconds(10)
                                .build());
    }

    private void verifyLooksForIframeOrErrors() {
        mocksToVerifyInOrder
                .verify(driver)
                .searchForFirstMatchingLocator(
                        BankIdElementsSearchQuery.builder()
                                .searchFor(LOC_IFRAME, LOC_ERROR_MESSAGE)
                                .build());
    }

    private void verifySetsSSNInput() {
        mocksToVerifyInOrder.verify(driver).setValueToElement(SAMPLE_SSN, LOC_SSN_INPUT);
    }

    private void verifyClicksSubmit() {
        mocksToVerifyInOrder.verify(driver).clickButton(LOC_SUBMIT_BUTTON);
    }
}
