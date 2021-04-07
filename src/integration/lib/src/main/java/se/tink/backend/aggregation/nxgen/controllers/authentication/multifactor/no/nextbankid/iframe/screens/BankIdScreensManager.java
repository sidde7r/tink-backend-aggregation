package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens;

import com.google.inject.Inject;
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

    public BankIdScreen waitForAnyScreenFromQuery(BankIdScreensQuery screensQuery) {

        List<BankIdElementLocator> screensLocators =
                screensQuery.getScreensToWaitFor().stream()
                        .map(BankIdScreen::getLocatorToDetectScreen)
                        .collect(Collectors.toList());

        BankIdElementsSearchResult searchResult =
                driver.searchForFirstMatchingLocator(
                        BankIdElementsSearchQuery.builder()
                                .searchFor(screensLocators)
                                .searchForSeconds(screensQuery.getSearchForSeconds())
                                .build());

        if (searchResult.isEmpty()) {
            BankIdScreen otherScreenFound = tryDetectCurrentScreen().orElse(null);
            errorHandler.throwUnexpectedScreenException(otherScreenFound);
        }

        return BankIdScreen.findScreenByItsLocator(searchResult.getLocatorFound());
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
                                .searchOnlyOnce(true)
                                .build());

        if (searchResult.isEmpty()) {
            return Optional.empty();
        }

        return BankIdScreen.tryFindScreenByItsLocator(searchResult.getLocatorFound());
    }
}
