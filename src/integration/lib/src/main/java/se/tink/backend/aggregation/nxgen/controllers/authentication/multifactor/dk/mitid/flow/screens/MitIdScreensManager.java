package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.screens;

import static java.util.Arrays.asList;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.MitIdConstants.MIT_ID_LOG_TAG;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.MitIdConstants.WaitTime.WAIT_TO_CHECK_IF_FOUND_SCREEN_WAS_NOT_REPLACED_WITH_ERROR_SCREEN;

import com.google.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.MitIdLocator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.MitIdLocatorsElements;
import se.tink.integration.webdriver.service.WebDriverService;
import se.tink.integration.webdriver.service.searchelements.ElementLocator;
import se.tink.integration.webdriver.service.searchelements.ElementsSearchQuery;
import se.tink.integration.webdriver.service.searchelements.ElementsSearchResult;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class MitIdScreensManager {

    private final WebDriverService driverService;
    private final MitIdLocatorsElements mitIdLocatorsElements;
    private final MitIdScreensErrorHandler screensErrorHandler;

    /**
     * Search for screens defined in {@link MitIdScreenQuery}.
     *
     * @return the very first screen that was found or throw exception if no screen was found.
     */
    public MitIdScreen searchForFirstScreen(MitIdScreenQuery screenQuery) {
        Optional<MitIdScreen> maybeScreenFound = trySearchForFirstScreen(screenQuery);
        if (maybeScreenFound.isPresent()) {
            return maybeScreenFound.get();
        }

        // search for any known screen
        Optional<MitIdScreen> maybeCurrentScreenFound =
                trySearchForFirstScreenInternal(
                        MitIdScreenQuery.builder()
                                .searchForExpectedScreens(MitIdScreen.values())
                                .searchOnlyOnce()
                                .build());
        /*
        Throw on any screen, even if it was expected in initial query - in that case,
        we should adjust the time of first search
         */
        throw screensErrorHandler.cannotFindScreenException(
                screenQuery, maybeCurrentScreenFound.orElse(null));
    }

    /**
     * Search for screens defined in {@link MitIdScreenQuery}.
     *
     * @return the very first screen that was found or empty.
     */
    public Optional<MitIdScreen> trySearchForFirstScreen(MitIdScreenQuery screenQuery) {
        MitIdScreen firstScreenFound = trySearchForFirstScreenInternal(screenQuery).orElse(null);
        if (firstScreenFound == null) {
            log.info("{} No screen found. Query: {}", MIT_ID_LOG_TAG, screenQuery);
            return Optional.empty();
        }
        log.info("{} Found a screen: {}. Query: {}", MIT_ID_LOG_TAG, firstScreenFound, screenQuery);

        /*
        Some screens may initially appear ok but after a brief moment an error screen
        appears instead. Since we might not know all such cases, we always double check for error screen.
        */
        int sleepFor = WAIT_TO_CHECK_IF_FOUND_SCREEN_WAS_NOT_REPLACED_WITH_ERROR_SCREEN * 1_000;
        log.info("{} Sleeping {} milliseconds before second search.", MIT_ID_LOG_TAG, sleepFor);
        driverService.sleepFor(sleepFor);

        MitIdScreenQuery secondQuery = screenQuery.toBuilder().searchOnlyOnce().build();
        MitIdScreen secondScreenFound = trySearchForFirstScreenInternal(secondQuery).orElse(null);
        if (secondScreenFound == null) {
            log.warn(
                    "{} No error or expected screen found. Second query: {}",
                    MIT_ID_LOG_TAG,
                    secondQuery);
            return Optional.empty();
        }
        log.info(
                "{} Found a second screen: {}. Second query: {}",
                MIT_ID_LOG_TAG,
                secondScreenFound,
                secondQuery);

        if (secondScreenFound != firstScreenFound) {
            log.warn(
                    "{} Second screen ({}) is not an error screen but is different from the first one ({})",
                    MIT_ID_LOG_TAG,
                    secondScreenFound,
                    firstScreenFound);
        }
        return Optional.of(secondScreenFound);
    }

    private Optional<MitIdScreen> trySearchForFirstScreenInternal(MitIdScreenQuery screenQuery) {
        List<MitIdScreen> expectedScreens = screenQuery.getExpectedScreensToSearchFor();
        List<ElementLocator> locatorsToSearchFor = getScreenLocators(expectedScreens);

        boolean isErrorScreenExpected =
                expectedScreens.contains(MitIdScreen.ERROR_NOTIFICATION_SCREEN);
        if (!isErrorScreenExpected) {
            locatorsToSearchFor.addAll(getScreenLocators(MitIdScreen.ERROR_NOTIFICATION_SCREEN));
        }

        List<MitIdScreen> screensFound = searchForAllScreens(locatorsToSearchFor, screenQuery);
        if (screensFound.contains(MitIdScreen.ERROR_NOTIFICATION_SCREEN)
                && !isErrorScreenExpected) {
            throw screensErrorHandler.unexpectedErrorScreenException(screenQuery);
        }

        List<MitIdScreen> expectedScreensFound =
                ListUtils.intersection(screensFound, expectedScreens);
        if (expectedScreensFound.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(expectedScreensFound.get(0));
    }

    private List<MitIdScreen> searchForAllScreens(
            List<ElementLocator> screenLocators, MitIdScreenQuery screenQuery) {
        List<ElementsSearchResult> searchResults =
                driverService.searchForAllMatchingLocators(
                        ElementsSearchQuery.builder()
                                .searchFor(screenLocators)
                                .searchForSeconds(screenQuery.getSearchForSeconds())
                                .build());
        return searchResults.stream()
                .map(
                        searchResult -> {
                            MitIdLocator locator =
                                    mitIdLocatorsElements.getMitIdLocatorByElementLocator(
                                            searchResult.getLocatorFound());
                            return MitIdScreen.getByMitIdLocator(locator);
                        })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private List<ElementLocator> getScreenLocators(MitIdScreen... screens) {
        return getScreenLocators(asList(screens));
    }

    private List<ElementLocator> getScreenLocators(List<MitIdScreen> screens) {
        return screens.stream()
                .map(MitIdScreen::getLocatorIdentifyingScreen)
                .map(mitIdLocatorsElements::getElementLocator)
                .collect(Collectors.toList());
    }
}
