package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.BankIdWebDriver;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.BankIdElementLocator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.BankIdElementsSearchQuery;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.BankIdElementsSearchResult;

@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class BankIdScreensManager {

    private final BankIdWebDriver driver;
    private final BankIdScreensErrorHandler errorHandler;

    /** Wait for any screen from query and throw error when it can't be found. */
    public BankIdScreen waitForAnyScreenFromQuery(BankIdScreensQuery screensQuery) {

        Optional<BankIdScreen> screenFound = tryWaitForAnyScreenFromQuery(screensQuery);

        if (!screenFound.isPresent()) {
            BankIdScreen otherScreenFound = tryDetectCurrentScreen().orElse(null);
            errorHandler.throwUnexpectedScreenException(
                    otherScreenFound, screensQuery.getScreensToWaitFor());
            // this is never reached, added to get rid of warnings
            return null;
        }

        return screenFound.get();
    }

    public Optional<BankIdScreen> tryWaitForAnyScreenFromQuery(BankIdScreensQuery screensQuery) {

        List<BankIdElementLocator> screensLocators =
                screensQuery.getScreensToWaitFor().stream()
                        .map(BankIdScreen::getLocatorToDetectScreen)
                        .collect(Collectors.toList());

        if (screensQuery.isShouldVerifyNoErrorScreens()) {
            screensLocators = addErrorScreenLocators(screensLocators);
        }

        BankIdElementsSearchResult searchResult =
                driver.searchForFirstMatchingLocator(
                        BankIdElementsSearchQuery.builder()
                                .searchFor(screensLocators)
                                .searchForSeconds(screensQuery.getWaitForSeconds())
                                .build());

        if (searchResult.isEmpty()) {
            return Optional.empty();
        }

        BankIdScreen screenFound =
                BankIdScreen.findScreenByItsLocator(searchResult.getLocatorFound());

        if (screenFound.isErrorScreen() && screensQuery.isShouldVerifyNoErrorScreens()) {
            errorHandler.throwUnexpectedScreenException(
                    screenFound, screensQuery.getScreensToWaitFor());
        }

        return Optional.of(screenFound);
    }

    private List<BankIdElementLocator> addErrorScreenLocators(List<BankIdElementLocator> locators) {
        List<BankIdElementLocator> resultList = new ArrayList<>(locators);

        BankIdScreen.getAllErrorScreens().stream()
                .map(BankIdScreen::getLocatorToDetectScreen)
                .filter(errorScreenLocator -> !resultList.contains(errorScreenLocator))
                .forEach(resultList::add);

        return resultList;
    }

    private Optional<BankIdScreen> tryDetectCurrentScreen() {

        List<BankIdElementLocator> screensLocators =
                BankIdScreen.getAllScreens().stream()
                        .map(BankIdScreen::getLocatorToDetectScreen)
                        .collect(Collectors.toList());

        BankIdElementsSearchResult searchResult =
                driver.searchForFirstMatchingLocator(
                        BankIdElementsSearchQuery.builder()
                                .searchFor(screensLocators)
                                .searchOnlyOnce()
                                .build());

        if (searchResult.isEmpty()) {
            return Optional.empty();
        }

        return BankIdScreen.tryFindScreenByItsLocator(searchResult.getLocatorFound());
    }
}
