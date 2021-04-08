package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreen.ENTER_SSN_SCREEN;

import java.util.List;
import java.util.stream.Collectors;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.openqa.selenium.WebElement;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.BankIdWebDriver;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.BankIdElementLocator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.BankIdElementsSearchQuery;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.BankIdElementsSearchResult;

@RunWith(JUnitParamsRunner.class)
public class BankIdScreensManagerTest {

    private static RuntimeException unexpectedScreenException;

    /*
    Mocks
     */
    private BankIdWebDriver driver;
    private BankIdScreensErrorHandler errorHandler;

    private InOrder mocksToVerifyInOrder;

    /*
    Real
     */
    private BankIdScreensManager screensManager;

    @Before
    public void setup() {
        driver = mock(BankIdWebDriver.class);

        unexpectedScreenException = mock(RuntimeException.class);
        errorHandler = mock(BankIdScreensErrorHandler.class);
        doThrow(unexpectedScreenException)
                .when(errorHandler)
                .throwUnexpectedScreenException(any(), any());

        mocksToVerifyInOrder = inOrder(driver, errorHandler);

        screensManager = new BankIdScreensManager(driver, errorHandler);
    }

    @Test
    @Parameters(method = "shouldMakeCorrectElementsSearchQuery")
    public void should_make_correct_elements_search_query_for_screen_locators(
            BankIdScreensQuery screensQuery,
            BankIdElementsSearchQuery expectedElementsSearchQuery) {
        // given
        // let's just find any screen, e.g. the first one - the exact screen doesn't matter
        BankIdScreen screenToBeFound = screensQuery.getScreensToWaitFor().get(0);
        when(driver.searchForFirstMatchingLocator(any()))
                .thenReturn(screenLocatorFoundResult(screenToBeFound));

        // when
        BankIdScreen screen = screensManager.waitForAnyScreenFromQuery(screensQuery);

        // then
        assertThat(screen).isEqualTo(screenToBeFound);

        ArgumentCaptor<BankIdElementsSearchQuery> captor =
                ArgumentCaptor.forClass(BankIdElementsSearchQuery.class);
        mocksToVerifyInOrder.verify(driver).searchForFirstMatchingLocator(captor.capture());
        assertThat(captor.getAllValues().size()).isEqualTo(1);
        assertThat(captor.getAllValues().get(0))
                .isEqualToComparingFieldByFieldRecursively(expectedElementsSearchQuery);

        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @SuppressWarnings("unused")
    private Object[] shouldMakeCorrectElementsSearchQuery() {
        return new Object[] {
            array(
                    BankIdScreensQuery.builder()
                            .waitForScreens(ENTER_SSN_SCREEN)
                            .waitForSeconds(10)
                            .build(),
                    BankIdElementsSearchQuery.builder()
                            .searchFor(getScreensLocators(ENTER_SSN_SCREEN))
                            .waitForSeconds(10)
                            .build()),
            array(
                    BankIdScreensQuery.builder()
                            .waitForScreens(ENTER_SSN_SCREEN)
                            .waitForSeconds(11)
                            .verifyNoErrorScreens(true)
                            .build(),
                    BankIdElementsSearchQuery.builder()
                            .searchFor(getScreensLocators(ENTER_SSN_SCREEN))
                            .searchFor(getScreensLocators(BankIdScreen.getAllErrorScreens()))
                            .waitForSeconds(11)
                            .build()),
            array(
                    BankIdScreensQuery.builder()
                            .waitForScreens(BankIdScreen.getAll2FAMethodScreens())
                            .waitForSeconds(123)
                            .build(),
                    BankIdElementsSearchQuery.builder()
                            .searchFor(getScreensLocators(BankIdScreen.getAll2FAMethodScreens()))
                            .waitForSeconds(123)
                            .build())
        };
    }

    @Test
    @Parameters(method = "allScreens")
    public void
            should_throw_unexpected_error_screen_exception_when_verify_no_error_screens_flag_is_on(
                    BankIdScreen screenToSearchFor) {
        // given
        BankIdScreen errorScreenToBeFound = BankIdScreen.getAllErrorScreens().get(0);
        when(driver.searchForFirstMatchingLocator(any()))
                .thenReturn(screenLocatorFoundResult(errorScreenToBeFound));

        // when
        Throwable throwable =
                catchThrowable(
                        () ->
                                screensManager.waitForAnyScreenFromQuery(
                                        BankIdScreensQuery.builder()
                                                .waitForScreens(screenToSearchFor)
                                                .verifyNoErrorScreens(true)
                                                .build()));

        // then
        assertThat(throwable).isEqualTo(unexpectedScreenException);

        mocksToVerifyInOrder
                .verify(errorHandler)
                .throwUnexpectedScreenException(
                        errorScreenToBeFound, singletonList(screenToSearchFor));
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    @Parameters(method = "allErrorScreens")
    public void
            should_not_throw_unexpected_error_screen_exception_when_verify_no_error_screens_flag_is_off(
                    BankIdScreen errorScreenToSearchFor) {
        // given
        when(driver.searchForFirstMatchingLocator(any()))
                .thenReturn(screenLocatorFoundResult(errorScreenToSearchFor));

        // when
        BankIdScreen screenFound =
                screensManager.waitForAnyScreenFromQuery(
                        BankIdScreensQuery.builder()
                                .waitForScreens(errorScreenToSearchFor)
                                .verifyNoErrorScreens(false)
                                .build());

        // then
        assertThat(screenFound).isEqualTo(errorScreenToSearchFor);

        mocksToVerifyInOrder.verify(driver).searchForFirstMatchingLocator(any());
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    @Parameters(method = "allScreens")
    public void
            when_no_expected_screen_is_found_should_search_again_to_detect_current_screen_and_then_throw_error(
                    BankIdScreen screenToSearchFor) {
        // given
        BankIdScreen otherScreenDetected = allScreensExcept(screenToSearchFor).get(0);
        when(driver.searchForFirstMatchingLocator(any()))
                // the first search is for screens from query
                .thenReturn(BankIdElementsSearchResult.empty())
                // the second one is for all screen selectors to detect any known screen
                .thenReturn(screenLocatorFoundResult(otherScreenDetected));

        RuntimeException notExpectedScreenError = mock(RuntimeException.class);
        doThrow(notExpectedScreenError)
                .when(errorHandler)
                .throwUnexpectedScreenException(any(), any());

        // when
        BankIdScreensQuery searchQuery =
                BankIdScreensQuery.builder()
                        .waitForScreens(screenToSearchFor)
                        .waitForSeconds(15)
                        .build();
        Throwable throwable =
                catchThrowable(() -> screensManager.waitForAnyScreenFromQuery(searchQuery));

        // then
        assertThat(throwable).isEqualTo(notExpectedScreenError);

        ArgumentCaptor<BankIdElementsSearchQuery> captor =
                ArgumentCaptor.forClass(BankIdElementsSearchQuery.class);
        mocksToVerifyInOrder
                .verify(driver, times(2))
                .searchForFirstMatchingLocator(captor.capture());

        assertThat(captor.getAllValues().size()).isEqualTo(2);
        assertThat(captor.getAllValues().get(0))
                .isEqualToComparingFieldByFieldRecursively(
                        BankIdElementsSearchQuery.builder()
                                .searchFor(screenToSearchFor.getLocatorToDetectScreen())
                                .waitForSeconds(15)
                                .build());
        assertThat(captor.getAllValues().get(1))
                .isEqualToComparingFieldByFieldRecursively(
                        BankIdElementsSearchQuery.builder()
                                .searchFor(
                                        BankIdScreen.getAllScreens().stream()
                                                .map(BankIdScreen::getLocatorToDetectScreen)
                                                .collect(toList()))
                                .waitForSeconds(0)
                                .build());

        mocksToVerifyInOrder
                .verify(errorHandler)
                .throwUnexpectedScreenException(
                        otherScreenDetected, singletonList(screenToSearchFor));
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @SuppressWarnings("unused")
    private static Object[] allScreens() {
        return BankIdScreen.getAllScreens().toArray();
    }

    @SuppressWarnings("unused")
    private static Object[] allErrorScreens() {
        return BankIdScreen.getAllErrorScreens().toArray();
    }

    private static List<BankIdElementLocator> getScreensLocators(BankIdScreen... screens) {
        return getScreensLocators(asList(screens));
    }

    private static List<BankIdElementLocator> getScreensLocators(List<BankIdScreen> screens) {
        return screens.stream()
                .map(BankIdScreen::getLocatorToDetectScreen)
                .collect(Collectors.toList());
    }

    private BankIdElementsSearchResult screenLocatorFoundResult(BankIdScreen bankIdScreen) {
        WebElement elementFound = mock(WebElement.class);
        if (bankIdScreen == null) {
            return BankIdElementsSearchResult.empty();
        }
        return BankIdElementsSearchResult.of(bankIdScreen.getLocatorToDetectScreen(), elementFound);
    }

    private static List<BankIdScreen> allScreensExcept(BankIdScreen excludedScreen) {
        return BankIdScreen.getAllScreens().stream()
                .filter(screen -> screen != excludedScreen)
                .collect(toList());
    }

    private static Object[] array(Object... args) {
        return args;
    }
}
