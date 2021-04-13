package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.BANK_ID_LOG_PREFIX;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdTestUtils.mockWebElementWithText;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdTestUtils.verifyThatFromUsersPerspectiveThrowableIsTheSameAsGivenAgentException;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lombok.Builder;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebElement;
import se.tink.backend.aggregation.agents.exceptions.bankidno.BankIdNOError;
import se.tink.backend.aggregation.agents.exceptions.bankidno.BankIdNOErrorCode;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.BankIdWebDriver;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.BankIdElementLocator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.BankIdElementsSearchQuery;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.BankIdElementsSearchResult;

@RunWith(JUnitParamsRunner.class)
public class BankIdScreensErrorHandlerTest {

    /*
    Mocks
     */
    private BankIdWebDriver webDriver;

    /*
    Real
     */
    private BankIdScreensErrorHandler errorHandler;

    @Before
    public void setup() {
        webDriver = mock(BankIdWebDriver.class);

        errorHandler = new BankIdScreensErrorHandler(webDriver);
    }

    @Test
    public void should_throw_correct_exception_when_unexpected_screen_is_null() {
        // when
        Throwable throwable =
                catchThrowable(
                        () -> errorHandler.throwUnexpectedScreenException(null, emptyList()));

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        BANK_ID_LOG_PREFIX
                                + " Could not find any known screen. (expectedScreens: %s)",
                        emptyList());
    }

    @Test
    @Parameters(method = "allNonErrorScreens")
    public void should_throw_correct_exception_when_unexpected_screen_is_not_an_error_screen(
            BankIdScreen screen) {
        // when
        Throwable throwable =
                catchThrowable(
                        () -> errorHandler.throwUnexpectedScreenException(screen, emptyList()));

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        String.format(
                                "%s Unexpected non error screen: %s (expectedScreens: %s)",
                                BANK_ID_LOG_PREFIX, screen, emptyList()));
    }

    @SuppressWarnings("unused")
    private static Object[] allNonErrorScreens() {
        return BankIdScreen.getAllNonErrorScreens().toArray();
    }

    @Test
    @Parameters(method = "allErrorScreensWithErrorTextLocators")
    public void should_throw_unknown_bank_id_exception_when_error_screen_text_cannot_be_found(
            BankIdScreen errorScreen, BankIdElementLocator errorTextLocator) {
        // given
        when(webDriver.searchForFirstMatchingLocator(any()))
                .thenReturn(BankIdElementsSearchResult.empty());

        // when
        Throwable throwable =
                catchThrowable(
                        () ->
                                errorHandler.throwUnexpectedScreenException(
                                        errorScreen, emptyList()));

        // then
        verifyThatFromUsersPerspectiveThrowableIsTheSameAsGivenAgentException(
                throwable, BankIdNOError.UNKNOWN_BANK_ID_ERROR.exception());

        verify(webDriver)
                .searchForFirstMatchingLocator(
                        BankIdElementsSearchQuery.builder()
                                .searchFor(errorTextLocator)
                                .searchForSeconds(10)
                                .build());
        verifyNoMoreInteractions(webDriver);
    }

    @SuppressWarnings("unused")
    private static Object[] allErrorScreensWithErrorTextLocators() {
        return BankIdScreen.ALL_ERROR_SCREENS_WITH_ERROR_TEXT_LOCATORS.entrySet().stream()
                .map(entry -> new Object[] {entry.getKey(), entry.getValue()})
                .toArray();
    }

    @Test
    @Parameters(method = "allErrorScreensWithErrorTextLocators")
    public void
            should_throw_unknown_bank_id_exception_when_no_known_error_code_is_found_in_screen_text(
                    BankIdScreen errorScreen, BankIdElementLocator errorTextLocator) {
        // given
        WebElement errorTextElement = mockWebElementWithText("!@$%#^$#&");
        when(webDriver.searchForFirstMatchingLocator(any()))
                .thenReturn(BankIdElementsSearchResult.of(errorTextLocator, errorTextElement));

        // when
        Throwable throwable =
                catchThrowable(
                        () ->
                                errorHandler.throwUnexpectedScreenException(
                                        errorScreen, emptyList()));

        // then
        verifyThatFromUsersPerspectiveThrowableIsTheSameAsGivenAgentException(
                throwable, BankIdNOError.UNKNOWN_BANK_ID_ERROR.exception());

        verify(webDriver)
                .searchForFirstMatchingLocator(
                        BankIdElementsSearchQuery.builder()
                                .searchFor(errorTextLocator)
                                .searchForSeconds(10)
                                .build());
        verifyNoMoreInteractions(webDriver);
    }

    @Test
    @Parameters(method = "errorCodeTestParams")
    public void should_throw_bank_id_exception_specific_for_error_code_found_in_screen_text(
            String errorScreenText, BankIdNOErrorCode expectedErrorCode) {
        // given
        BankIdScreen exampleErrorScreen = BankIdScreen.BANK_ID_ERROR_WITH_HEADING_SCREEN;
        BankIdElementLocator errorTextLocator =
                BankIdScreen.ALL_ERROR_SCREENS_WITH_ERROR_TEXT_LOCATORS.get(exampleErrorScreen);

        WebElement errorTextElement = mockWebElementWithText(errorScreenText);
        when(webDriver.searchForFirstMatchingLocator(any()))
                .thenReturn(BankIdElementsSearchResult.of(errorTextLocator, errorTextElement));

        // when
        Throwable throwable =
                catchThrowable(
                        () ->
                                errorHandler.throwUnexpectedScreenException(
                                        exampleErrorScreen, emptyList()));

        // then
        verifyThatFromUsersPerspectiveThrowableIsTheSameAsGivenAgentException(
                throwable, expectedErrorCode.getError().exception());

        verify(webDriver)
                .searchForFirstMatchingLocator(
                        BankIdElementsSearchQuery.builder()
                                .searchFor(errorTextLocator)
                                .searchForSeconds(10)
                                .build());
        verifyNoMoreInteractions(webDriver);
    }

    @SuppressWarnings("unused")
    private static Object[] errorCodeTestParams() {

        Stream<ErrorCodeTestParams> testParams =
                Stream.of(BankIdNOErrorCode.values())
                        .map(
                                errorCode ->
                                        ErrorCodeTestParams.builder()
                                                .errorText(errorCode.getCode())
                                                .expectedErrorCode(errorCode)
                                                .build());

        return testParams
                .map(
                        params ->
                                Arrays.asList(
                                        params.modifyErrorText(String::toUpperCase),
                                        params.modifyErrorText(String::toLowerCase),
                                        params.modifyErrorText(StringUtils::capitalize),
                                        params.modifyErrorText(text -> "!@$#%$^%  &^%*" + text),
                                        params.modifyErrorText(text -> text + "@!%!@%")))
                .flatMap(List::stream)
                .map(ErrorCodeTestParams::toMethodParams)
                .toArray();
    }

    @Builder
    private static class ErrorCodeTestParams {
        private final String errorText;
        private final BankIdNOErrorCode expectedErrorCode;

        private ErrorCodeTestParams modifyErrorText(Function<String, String> errorTextModifier) {
            String newErrorText = errorTextModifier.apply(errorText);
            return new ErrorCodeTestParams(newErrorText, expectedErrorCode);
        }

        private Object[] toMethodParams() {
            return new Object[] {errorText, expectedErrorCode};
        }
    }
}
