package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.screens;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.MitIdConstants.WaitTime.WAIT_TO_CHECK_IF_FOUND_SCREEN_WAS_NOT_REPLACED_WITH_ERROR_SCREEN;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.screens.MitIdScreen.ERROR_NOTIFICATION_SCREEN;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.stubbing.answers.ReturnsElementsOf;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.MitIdLocators;
import se.tink.integration.webdriver.service.WebDriverService;
import se.tink.integration.webdriver.service.searchelements.ElementLocator;
import se.tink.integration.webdriver.service.searchelements.ElementsSearchQuery;
import se.tink.integration.webdriver.service.searchelements.ElementsSearchResult;

@RunWith(JUnitParamsRunner.class)
public class MitIdScreensManagerTest {

    private static final MitIdScreen SCREEN_1 = MitIdScreen.USER_ID_SCREEN;
    private static final MitIdScreen SCREEN_2 = MitIdScreen.CODE_APP_SCREEN;
    private static final MitIdScreen NOT_EXPECTED_SCREEN = MitIdScreen.CPR_SCREEN;

    private static final RuntimeException CANNOT_FIND_SCREEN_EXCEPTION =
            new RuntimeException("Cannot find screen");
    private static final RuntimeException UNEXPECTED_ERROR_SCREEN_EXCEPTION =
            new RuntimeException("Unexpected error screen");

    private WebDriverService driverService;
    private MitIdLocators mitIdLocators;
    private MitIdScreensErrorHandler screensErrorHandler;

    private MitIdScreensManager screensManager;

    @Before
    public void setup() {
        driverService = mock(WebDriverService.class);
        mitIdLocators = new MitIdLocators();

        screensErrorHandler = mock(MitIdScreensErrorHandler.class);
        when(screensErrorHandler.cannotFindScreenException(any(), any()))
                .thenReturn(CANNOT_FIND_SCREEN_EXCEPTION);
        when(screensErrorHandler.unexpectedErrorScreenException(any()))
                .thenReturn(UNEXPECTED_ERROR_SCREEN_EXCEPTION);

        screensManager = new MitIdScreensManager(driverService, mitIdLocators, screensErrorHandler);
    }

    /*
    Expected screen: found
    Error screen: not expected, not found
     */

    @Test
    public void should_try_search_and_return_expected_screen_that_was_found_twice() {
        // given
        MitIdScreenQuery query =
                MitIdScreenQuery.builder()
                        .searchForExpectedScreens(SCREEN_1, SCREEN_2)
                        .searchForSeconds(10)
                        .build();
        mockScreensFound(
                asList(NOT_EXPECTED_SCREEN, SCREEN_1, SCREEN_2),
                asList(NOT_EXPECTED_SCREEN, SCREEN_1, SCREEN_2));

        // when
        Optional<MitIdScreen> maybeScreen = screensManager.trySearchForFirstScreen(query);

        // then
        assertThat(maybeScreen).hasValue(SCREEN_1);

        verifySearchesFor(asList(SCREEN_1, SCREEN_2, ERROR_NOTIFICATION_SCREEN), 10);
        verifySleepsBetweenSecondSearch();
        verifySearchesFor(asList(SCREEN_1, SCREEN_2, ERROR_NOTIFICATION_SCREEN), 0);
        verifyNoMoreInteractions(driverService);
    }

    @Test
    public void should_search_and_return_expected_screen_that_was_found_twice() {
        // given
        MitIdScreenQuery query =
                MitIdScreenQuery.builder()
                        .searchForExpectedScreens(SCREEN_2, SCREEN_1)
                        .searchForSeconds(10)
                        .build();
        mockScreensFound(
                asList(NOT_EXPECTED_SCREEN, SCREEN_2, SCREEN_1),
                asList(NOT_EXPECTED_SCREEN, SCREEN_2, SCREEN_1));

        // when
        MitIdScreen screen = screensManager.searchForFirstScreen(query);

        // then
        assertThat(screen).isEqualTo(SCREEN_2);

        verifySearchesFor(asList(SCREEN_2, SCREEN_1, ERROR_NOTIFICATION_SCREEN), 10);
        verifySleepsBetweenSecondSearch();
        verifySearchesFor(asList(SCREEN_2, SCREEN_1, ERROR_NOTIFICATION_SCREEN), 0);
        verifyNoMoreInteractions(driverService);
    }

    /*
    Expected screen: not found
    Error screen: not expected, not found
     */

    @Test
    @Parameters(method = "paramsForEmptyScreenSearchResult")
    public void should_try_search_and_return_expected_screen_not_found(
            List<MitIdScreen> otherScreensFound) {
        // given
        MitIdScreenQuery query =
                MitIdScreenQuery.builder()
                        .searchForExpectedScreens(SCREEN_1)
                        .searchForSeconds(10)
                        .build();
        mockScreensFound(otherScreensFound);

        // when
        Optional<MitIdScreen> maybeScreen = screensManager.trySearchForFirstScreen(query);

        // then
        assertThat(maybeScreen).isEmpty();

        verifySearchesFor(asList(SCREEN_1, ERROR_NOTIFICATION_SCREEN), 10);
        verifyNoMoreInteractions(driverService);
    }

    @Test
    @Parameters(method = "paramsForEmptyScreenSearchResult")
    @SuppressWarnings("all")
    public void should_search_and_and_throw_screen_not_found_with_current_screen_detected(
            List<MitIdScreen> otherScreensFound) {
        // given
        MitIdScreenQuery query =
                MitIdScreenQuery.builder()
                        .searchForExpectedScreens(SCREEN_1)
                        .searchForSeconds(10)
                        .build();
        mockScreensFound(otherScreensFound, otherScreensFound);

        // when
        Throwable throwable = catchThrowable(() -> screensManager.searchForFirstScreen(query));

        // then
        assertThat(throwable).isEqualTo(CANNOT_FIND_SCREEN_EXCEPTION);

        verifySearchesFor(asList(SCREEN_1, ERROR_NOTIFICATION_SCREEN), 10);
        verifySearchesFor(asList(MitIdScreen.values()), 0);
        verifyNoMoreInteractions(driverService);

        verify(screensErrorHandler)
                .cannotFindScreenException(eq(query), eq(firstElementOrNull(otherScreensFound)));
    }

    @Test
    @Parameters(method = "paramsForEmptyScreenSearchResult")
    @SuppressWarnings("all")
    public void
            should_search_and_and_throw_screen_not_found_with_current_screen_detected_even_if_it_was_expected_in_query(
                    List<MitIdScreen> otherScreensFound) {
        // given
        MitIdScreenQuery query =
                MitIdScreenQuery.builder()
                        .searchForExpectedScreens(SCREEN_1)
                        .searchForSeconds(10)
                        .build();
        mockScreensFound(otherScreensFound, asList(SCREEN_1));

        // when
        Throwable throwable = catchThrowable(() -> screensManager.searchForFirstScreen(query));

        // then
        assertThat(throwable).isEqualTo(CANNOT_FIND_SCREEN_EXCEPTION);

        verifySearchesFor(asList(SCREEN_1, ERROR_NOTIFICATION_SCREEN), 10);
        verifySearchesFor(asList(MitIdScreen.values()), 0);
        verifyNoMoreInteractions(driverService);

        verify(screensErrorHandler).cannotFindScreenException(eq(query), eq(SCREEN_1));
    }

    private static <T> T firstElementOrNull(List<T> list) {
        if (!list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    @SuppressWarnings("unused")
    private static Object[] paramsForEmptyScreenSearchResult() {
        return new Object[] {
            new Object[] {emptyList()}, new Object[] {singletonList(NOT_EXPECTED_SCREEN)}
        };
    }

    /*
    Expected screen: found
    Error screen: expected, found
     */

    @Test
    public void should_try_search_and_return_expected_error_screen() {
        // given
        MitIdScreenQuery query =
                MitIdScreenQuery.builder()
                        .searchForExpectedScreens(SCREEN_1, ERROR_NOTIFICATION_SCREEN)
                        .searchForSeconds(10)
                        .build();
        mockScreensFound(
                singletonList(ERROR_NOTIFICATION_SCREEN), singletonList(ERROR_NOTIFICATION_SCREEN));

        // when
        Optional<MitIdScreen> maybeScreen = screensManager.trySearchForFirstScreen(query);

        // then
        assertThat(maybeScreen).hasValue(ERROR_NOTIFICATION_SCREEN);

        verifySearchesFor(asList(SCREEN_1, ERROR_NOTIFICATION_SCREEN), 10);
        verifySleepsBetweenSecondSearch();
        verifySearchesFor(asList(SCREEN_1, ERROR_NOTIFICATION_SCREEN), 0);
        verifyNoMoreInteractions(driverService);
    }

    @Test
    public void should_search_and_return_expected_error_screen() {
        // given
        MitIdScreenQuery query =
                MitIdScreenQuery.builder()
                        .searchForExpectedScreens(SCREEN_1, ERROR_NOTIFICATION_SCREEN)
                        .searchForSeconds(10)
                        .build();
        mockScreensFound(
                singletonList(ERROR_NOTIFICATION_SCREEN), singletonList(ERROR_NOTIFICATION_SCREEN));

        // when
        MitIdScreen screen = screensManager.searchForFirstScreen(query);

        // then
        assertThat(screen).isEqualTo(ERROR_NOTIFICATION_SCREEN);

        verifySearchesFor(asList(SCREEN_1, ERROR_NOTIFICATION_SCREEN), 10);
        verifySleepsBetweenSecondSearch();
        verifySearchesFor(asList(SCREEN_1, ERROR_NOTIFICATION_SCREEN), 0);
        verifyNoMoreInteractions(driverService);
    }

    /*
    Expected screen: not found
    Error screen: not expected, found
     */
    @Test
    public void should_try_search_and_throw_unexpected_error_screen_from_first_search() {
        // given
        MitIdScreenQuery query =
                MitIdScreenQuery.builder()
                        .searchForExpectedScreens(SCREEN_1)
                        .searchForSeconds(10)
                        .build();
        mockScreensFound(singletonList(ERROR_NOTIFICATION_SCREEN));

        // when
        Throwable throwable = catchThrowable(() -> screensManager.trySearchForFirstScreen(query));

        // then
        assertThat(throwable).isEqualTo(UNEXPECTED_ERROR_SCREEN_EXCEPTION);

        verifySearchesFor(asList(SCREEN_1, ERROR_NOTIFICATION_SCREEN), 10);
        verifyNoMoreInteractions(driverService);
    }

    @Test
    public void should_try_search_and_throw_unexpected_error_screen_from_second_search() {
        // given
        MitIdScreenQuery query =
                MitIdScreenQuery.builder()
                        .searchForExpectedScreens(SCREEN_1)
                        .searchForSeconds(10)
                        .build();
        mockScreensFound(singletonList(SCREEN_1), singletonList(ERROR_NOTIFICATION_SCREEN));

        // when
        Throwable throwable = catchThrowable(() -> screensManager.trySearchForFirstScreen(query));

        // then
        assertThat(throwable).isEqualTo(UNEXPECTED_ERROR_SCREEN_EXCEPTION);

        verifySearchesFor(asList(SCREEN_1, ERROR_NOTIFICATION_SCREEN), 10);
        verifySleepsBetweenSecondSearch();
        verifySearchesFor(asList(SCREEN_1, ERROR_NOTIFICATION_SCREEN), 0);
        verifyNoMoreInteractions(driverService);
    }

    @Test
    public void should_search_and_throw_unexpected_error_screen_from_first_search() {
        // given
        MitIdScreenQuery query =
                MitIdScreenQuery.builder()
                        .searchForExpectedScreens(SCREEN_1)
                        .searchForSeconds(10)
                        .build();
        mockScreensFound(singletonList(ERROR_NOTIFICATION_SCREEN));

        // when
        Throwable throwable = catchThrowable(() -> screensManager.searchForFirstScreen(query));

        // then
        assertThat(throwable).isEqualTo(UNEXPECTED_ERROR_SCREEN_EXCEPTION);

        verifySearchesFor(asList(SCREEN_1, ERROR_NOTIFICATION_SCREEN), 10);
        verifyNoMoreInteractions(driverService);
    }

    @Test
    public void should_search_and_throw_unexpected_error_screen_from_second_search() {
        // given
        MitIdScreenQuery query =
                MitIdScreenQuery.builder()
                        .searchForExpectedScreens(SCREEN_1)
                        .searchForSeconds(10)
                        .build();
        mockScreensFound(singletonList(SCREEN_1), singletonList(ERROR_NOTIFICATION_SCREEN));

        // when
        Throwable throwable = catchThrowable(() -> screensManager.searchForFirstScreen(query));

        // then
        assertThat(throwable).isEqualTo(UNEXPECTED_ERROR_SCREEN_EXCEPTION);

        verifySearchesFor(asList(SCREEN_1, ERROR_NOTIFICATION_SCREEN), 10);
        verifySleepsBetweenSecondSearch();
        verifySearchesFor(asList(SCREEN_1, ERROR_NOTIFICATION_SCREEN), 0);
        verifyNoMoreInteractions(driverService);
    }

    /*
    Expected screen: found
    Error screen: not expected, found
     */
    @Test
    public void
            should_try_search_and_throw_unexpected_error_screen_even_if_expected_screen_was_found() {
        // given
        MitIdScreenQuery query =
                MitIdScreenQuery.builder()
                        .searchForExpectedScreens(SCREEN_1)
                        .searchForSeconds(10)
                        .build();
        mockScreensFound(asList(SCREEN_1, ERROR_NOTIFICATION_SCREEN));

        // when
        Throwable throwable = catchThrowable(() -> screensManager.trySearchForFirstScreen(query));

        // then
        assertThat(throwable).isEqualTo(UNEXPECTED_ERROR_SCREEN_EXCEPTION);

        verifySearchesFor(asList(SCREEN_1, ERROR_NOTIFICATION_SCREEN), 10);
        verifyNoMoreInteractions(driverService);
    }

    @Test
    public void
            should_search_and_throw_unexpected_error_screen_even_if_expected_screen_was_found() {
        // given
        MitIdScreenQuery query =
                MitIdScreenQuery.builder()
                        .searchForExpectedScreens(SCREEN_1)
                        .searchForSeconds(10)
                        .build();
        mockScreensFound(asList(SCREEN_1, ERROR_NOTIFICATION_SCREEN));

        // when
        Throwable throwable = catchThrowable(() -> screensManager.searchForFirstScreen(query));

        // then
        assertThat(throwable).isEqualTo(UNEXPECTED_ERROR_SCREEN_EXCEPTION);

        verifySearchesFor(asList(SCREEN_1, ERROR_NOTIFICATION_SCREEN), 10);
        verifyNoMoreInteractions(driverService);
    }

    @SafeVarargs
    private final void mockScreensFound(List<MitIdScreen>... screensFound) {
        List<List<ElementsSearchResult>> results =
                Stream.of(screensFound).map(this::screensFoundResult).collect(Collectors.toList());

        when(driverService.searchForAllMatchingLocators(any()))
                .thenAnswer(new ReturnsElementsOf(results));
    }

    private void verifySearchesFor(List<MitIdScreen> screens, int time) {
        ElementsSearchQuery.ElementsSearchQueryBuilder builder = ElementsSearchQuery.builder();
        screens.forEach(screen -> builder.searchFor(getScreenLocator(screen)));
        builder.searchForSeconds(time);
        verify(driverService).searchForAllMatchingLocators(builder.build());
    }

    private void verifySleepsBetweenSecondSearch() {
        verify(driverService)
                .sleepFor(WAIT_TO_CHECK_IF_FOUND_SCREEN_WAS_NOT_REPLACED_WITH_ERROR_SCREEN * 1_000);
    }

    private List<ElementsSearchResult> screensFoundResult(List<MitIdScreen> screens) {
        return screens.stream()
                .map(s -> mitIdLocators.getElementLocator(s.getLocatorIdentifyingScreen()))
                .map(ElementsSearchResult::of)
                .collect(Collectors.toList());
    }

    private ElementLocator getScreenLocator(MitIdScreen screen) {
        return mitIdLocators.getElementLocator(screen.getLocatorIdentifyingScreen());
    }
}
