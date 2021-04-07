package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens;

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
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreen.BANK_ID_APP_METHOD_SCREEN;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreen.ENTER_SSN_SCREEN;

import java.util.List;
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
        errorHandler = mock(BankIdScreensErrorHandler.class);

        mocksToVerifyInOrder = inOrder(driver, errorHandler);

        screensManager = new BankIdScreensManager(driver, errorHandler);
    }

    @Test
    @Parameters(method = "searchOnlyForScreenElementsTestParams")
    public void should_search_only_for_screens_defined_in_query(
            List<BankIdScreen> screensToSearchFor,
            List<BankIdElementLocator> expectedLocatorToSearchFor) {
        // given
        // let's just find any screen, e.g. the first one - the exact screen doesn't matter
        BankIdScreen screenToBeFound = screensToSearchFor.get(0);
        when(driver.searchForFirstMatchingLocator(any()))
                .thenReturn(screenLocatorFoundResult(screenToBeFound));

        // when
        BankIdScreen screen =
                screensManager.waitForAnyScreenFromQuery(
                        BankIdScreensQuery.builder()
                                .waitForScreens(screensToSearchFor)
                                .waitForSeconds(10)
                                .build());

        // then
        assertThat(screen).isEqualTo(screenToBeFound);

        ArgumentCaptor<BankIdElementsSearchQuery> captor =
                ArgumentCaptor.forClass(BankIdElementsSearchQuery.class);
        mocksToVerifyInOrder.verify(driver).searchForFirstMatchingLocator(captor.capture());

        assertThat(captor.getAllValues().size()).isEqualTo(1);
        assertThat(captor.getAllValues().get(0))
                .isEqualToComparingFieldByFieldRecursively(
                        BankIdElementsSearchQuery.builder()
                                .searchFor(expectedLocatorToSearchFor)
                                .searchForSeconds(10)
                                .build());

        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @SuppressWarnings("unused")
    private Object[] searchOnlyForScreenElementsTestParams() {
        return new Object[] {
            array(
                    singletonList(ENTER_SSN_SCREEN),
                    singletonList(ENTER_SSN_SCREEN.getLocatorToDetectScreen())),
            array(
                    singletonList(BANK_ID_APP_METHOD_SCREEN),
                    singletonList(BANK_ID_APP_METHOD_SCREEN.getLocatorToDetectScreen())),
            array(
                    BankIdScreen.getAll2FAMethodScreens(),
                    BankIdScreen.getAll2FAMethodScreens().stream()
                            .map(BankIdScreen::getLocatorToDetectScreen)
                            .collect(toList()))
        };
    }

    @Test
    @Parameters(value = {"10", "15", "20"})
    public void should_search_for_screens_for_seconds_defined_in_query(int waitForSeconds) {
        // given
        when(driver.searchForFirstMatchingLocator(any()))
                .thenReturn(screenLocatorFoundResult(ENTER_SSN_SCREEN));

        // when
        BankIdScreen screen =
                screensManager.waitForAnyScreenFromQuery(
                        BankIdScreensQuery.builder()
                                .waitForScreens(ENTER_SSN_SCREEN)
                                .waitForSeconds(waitForSeconds)
                                .build());

        // then
        assertThat(screen).isEqualTo(ENTER_SSN_SCREEN);

        ArgumentCaptor<BankIdElementsSearchQuery> captor =
                ArgumentCaptor.forClass(BankIdElementsSearchQuery.class);
        mocksToVerifyInOrder.verify(driver).searchForFirstMatchingLocator(captor.capture());

        assertThat(captor.getAllValues().size()).isEqualTo(1);
        assertThat(captor.getAllValues().get(0))
                .isEqualToComparingFieldByFieldRecursively(
                        BankIdElementsSearchQuery.builder()
                                .searchFor(ENTER_SSN_SCREEN.getLocatorToDetectScreen())
                                .searchForSeconds(waitForSeconds)
                                .build());

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
        doThrow(notExpectedScreenError).when(errorHandler).throwUnexpectedScreenException(any());

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
                                .searchForSeconds(15)
                                .build());
        assertThat(captor.getAllValues().get(1))
                .isEqualToComparingFieldByFieldRecursively(
                        BankIdElementsSearchQuery.builder()
                                .searchFor(
                                        BankIdScreen.getAllScreens().stream()
                                                .map(BankIdScreen::getLocatorToDetectScreen)
                                                .collect(toList()))
                                .searchOnlyOnce(true)
                                .build());

        mocksToVerifyInOrder
                .verify(errorHandler)
                .throwUnexpectedScreenException(otherScreenDetected);
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @SuppressWarnings("unused")
    private static Object[] allScreens() {
        return BankIdScreen.getAllScreens().toArray();
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
