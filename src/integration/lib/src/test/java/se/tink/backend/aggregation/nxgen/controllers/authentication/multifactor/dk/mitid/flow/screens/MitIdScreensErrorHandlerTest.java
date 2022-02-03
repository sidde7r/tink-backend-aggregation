package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.screens;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.MitIdLocator.LOC_ERROR_NOTIFICATION;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lombok.Builder;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebElement;
import se.tink.backend.aggregation.agents.exceptions.mitid.MitIdError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.MitIdConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.MitIdLocators;
import se.tink.integration.webdriver.service.WebDriverService;
import se.tink.integration.webdriver.service.searchelements.ElementLocator;
import se.tink.integration.webdriver.service.searchelements.ElementsSearchQuery;
import se.tink.integration.webdriver.service.searchelements.ElementsSearchResult;

@RunWith(JUnitParamsRunner.class)
public class MitIdScreensErrorHandlerTest {

    private WebDriverService driverService;
    private MitIdLocators locators;

    private MitIdScreensErrorHandler errorHandler;

    @Before
    public void setup() {
        driverService = mock(WebDriverService.class);
        locators = mock(MitIdLocators.class);

        errorHandler = new MitIdScreensErrorHandler(driverService, locators);
    }

    @Test
    @Parameters(method = "paramsForShouldThrowIllegalStateExceptionOnCannotFindScreen")
    public void should_throw_illegal_state_exception_on_cannot_find_screen(
            MitIdScreenQuery query, MitIdScreen currentScreen, String expectedMessage) {
        // when
        Throwable throwable = errorHandler.cannotFindScreenException(query, currentScreen);

        // then
        assertThat(throwable).isInstanceOf(IllegalStateException.class).hasMessage(expectedMessage);
    }

    @SuppressWarnings("unused")
    private static Object[] paramsForShouldThrowIllegalStateExceptionOnCannotFindScreen() {
        return new Object[] {
            asArray(
                    MitIdScreenQuery.builder()
                            .searchForExpectedScreens(
                                    MitIdScreen.USER_ID_SCREEN, MitIdScreen.CODE_APP_SCREEN)
                            .build(),
                    MitIdScreen.CPR_SCREEN,
                    "\nCould not find expected screens"
                            + "\nExpected screens: [USER_ID_SCREEN, CODE_APP_SCREEN]"
                            + "\nCurrent screen: [CPR_SCREEN]"),
            asArray(
                    MitIdScreenQuery.builder()
                            .searchForExpectedScreens(MitIdScreen.CODE_APP_SCREEN)
                            .build(),
                    null,
                    "\nCould not find expected screens"
                            + "\nExpected screens: [CODE_APP_SCREEN]"
                            + "\nCurrent screen: [null]")
        };
    }

    @Test
    @Parameters(method = "paramsForShouldRecognizeCorrectError")
    public void should_throw_correct_mit_id_error_on_unexpected_error_screen(
            String errorMessage, MitIdError expectedError) {
        // given
        ElementLocator errorElementLocator = mock(ElementLocator.class);
        when(locators.getElementLocator(LOC_ERROR_NOTIFICATION)).thenReturn(errorElementLocator);

        WebElement errorElement = elementWithTextContent(errorMessage);
        when(driverService.searchForFirstMatchingLocator(any()))
                .thenReturn(ElementsSearchResult.of(errorElementLocator, errorElement));

        // when
        Throwable throwable =
                errorHandler.unexpectedErrorScreenException(mock(MitIdScreenQuery.class));

        // then
        assertThat(throwable).isInstanceOf(expectedError.exception().getClass());

        verify(driverService)
                .searchForFirstMatchingLocator(
                        ElementsSearchQuery.builder()
                                .searchFor(locators.getElementLocator(LOC_ERROR_NOTIFICATION))
                                .searchOnlyOnce()
                                .build());
    }

    @SuppressWarnings("unused")
    private static Object[] paramsForShouldRecognizeCorrectError() {
        List<ErrorCodeTestParams> paramsWithKnownErrors =
                MitIdConstants.Errors.ERROR_MESSAGE_MAPPING.entries().stream()
                        .map(
                                entry ->
                                        ErrorCodeTestParams.builder()
                                                .expectedError(entry.getKey())
                                                .errorText(entry.getValue())
                                                .build())
                        .map(
                                params ->
                                        Arrays.asList(
                                                params.modifyErrorText(String::toUpperCase),
                                                params.modifyErrorText(String::toLowerCase),
                                                params.modifyErrorText(StringUtils::capitalize),
                                                params.modifyErrorText(
                                                        text -> "!@$#%$^%  &^%*" + text),
                                                params.modifyErrorText(text -> text + "@!%!@%"),
                                                params.modifyErrorText(
                                                        text ->
                                                                "Prefix line \n"
                                                                        + text
                                                                        + "\n Suffix line"),
                                                params.modifyErrorText(text -> "unknown text")))
                        .flatMap(List::stream)
                        .collect(Collectors.toList());

        List<ErrorCodeTestParams> paramsWithUnknownErrors =
                Stream.of(
                                ErrorCodeTestParams.builder()
                                        .errorText("unknown text")
                                        .expectedError(MitIdError.UNKNOWN_ERROR_NOTIFICATION)
                                        .build(),
                                ErrorCodeTestParams.builder()
                                        .errorText("")
                                        .expectedError(MitIdError.UNKNOWN_ERROR_NOTIFICATION)
                                        .build())
                        .collect(Collectors.toList());

        return Stream.of(paramsWithKnownErrors, paramsWithUnknownErrors)
                .flatMap(List::stream)
                .map(ErrorCodeTestParams::toMethodParams)
                .toArray();
    }

    @Builder
    private static class ErrorCodeTestParams {
        private final String errorText;
        private final MitIdError expectedError;

        private ErrorCodeTestParams modifyErrorText(Function<String, String> errorTextModifier) {
            String newErrorText = errorTextModifier.apply(errorText);
            return new ErrorCodeTestParams(newErrorText, expectedError);
        }

        private Object[] toMethodParams() {
            return new Object[] {errorText, expectedError};
        }
    }

    @Test
    @Parameters(method = "paramsForShouldThrowMitIdErrorWithCorrectMessage")
    public void should_throw_mit_id_error_with_correct_message_on_unexpected_error_screen(
            MitIdScreenQuery query, String errorScreenText, String expectedExceptionMessage) {
        // given
        ElementLocator errorElementLocator = mock(ElementLocator.class);
        when(locators.getElementLocator(LOC_ERROR_NOTIFICATION)).thenReturn(errorElementLocator);

        WebElement errorElement = elementWithTextContent(errorScreenText);
        when(driverService.searchForFirstMatchingLocator(any()))
                .thenReturn(ElementsSearchResult.of(errorElementLocator, errorElement));

        // when
        Throwable throwable = errorHandler.unexpectedErrorScreenException(query);

        // then
        assertThat(throwable).hasMessageContaining(expectedExceptionMessage);
    }

    @SuppressWarnings("unused")
    private static Object[] paramsForShouldThrowMitIdErrorWithCorrectMessage() {
        return new Object[] {
            asArray(
                    MitIdScreenQuery.builder()
                            .searchForExpectedScreens(
                                    MitIdScreen.CODE_APP_SCREEN, MitIdScreen.USER_ID_SCREEN)
                            .build(),
                    "\tFirst line  \n\n \n Second line \t\t",
                    "\nUnexpected error screen found"
                            + "\nError message: [First line /n Second line]"
                            + "\nExpected screens: [CODE_APP_SCREEN, USER_ID_SCREEN]"),
            asArray(
                    MitIdScreenQuery.builder()
                            .searchForExpectedScreens(MitIdScreen.CODE_APP_SCREEN)
                            .build(),
                    "\tFirst line  \n\n \n Second line... \t ... \t\t",
                    "\nUnexpected error screen found"
                            + "\nError message: [First line /n Second line... \t ...]"
                            + "\nExpected screens: [CODE_APP_SCREEN]")
        };
    }

    private static WebElement elementWithTextContent(String textContent) {
        WebElement element = mock(WebElement.class);
        when(element.getAttribute("textContent")).thenReturn(textContent);
        return element;
    }

    private static Object[] asArray(Object... objects) {
        return objects;
    }
}
