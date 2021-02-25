package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.choosemethod;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.NEMID_LINK_TO_SELECT_DIFFERENT_2FA_METHOD;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.NEMID_SELECT_METHOD_POPUP;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemId2FAMethod;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemId2FAMethodScreen;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.utils.ElementsSearchQuery;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.utils.ElementsSearchResult;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.utils.MultipleElementsSearchResult;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.utils.NemIdWebDriverWrapper;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class NemIdDetect2FAMethodsStep {

    public static final List<By> ELEMENTS_TO_SEARCH_FOR_AFTER_CLICKING_CHANGE_METHOD_LINK =
            ImmutableList.<By>builder()
                    .add(NEMID_SELECT_METHOD_POPUP)
                    .addAll(NemId2FAMethodScreen.getSelectorsForAllScreens())
                    .build();

    private final NemIdWebDriverWrapper driverWrapper;

    /**
     * Tries to interact with an iframe to check which 2FA methods are available to choose. NOTE:
     * after detection process iframe may be left on a different screen than the default one
     */
    public NemIdDetect2FAMethodsResult detect2FAMethods(NemId2FAMethodScreen defaultScreen) {

        if (!linkToChange2FAMethodExists()) {
            // It's a guess that if user has only 1 NemID method available there might not be any
            // link to change it
            log.info(
                    "Cannot find link to change 2FA method. Check page source:\n{}",
                    driverWrapper.getFullPageSourceLog());
            return NemIdDetect2FAMethodsResult.canOnlyUseDefaultMethod(defaultScreen);
        }

        /*
        Depending on how many 2FA methods user have the change method link behaves differently:
        - for 2 methods it toggles between the only 2 possible 2FA screens
        - for 3 methods (or possibly more) it doesn't change screen but opens a popup instead - this popup allows
          to choose and switch to the screen dedicated for one of the 2 remaining methods
         */
        clickChange2FAMethodLink();
        By elementFound = searchForScreenOrPopup();

        Optional<NemId2FAMethodScreen> maybeMethodScreen =
                NemId2FAMethodScreen.getScreenBySelector(elementFound);

        if (maybeMethodScreen.isPresent()) {
            NemId2FAMethodScreen currentScreen = maybeMethodScreen.get();

            if (currentScreen == defaultScreen) {
                // It's a guess that if user has only 1 NemID method available there might be a link
                // but it doesn't change it
                log.info(
                        "Link to change 2FA method does not work. Check page source:\n{}",
                        driverWrapper.getFullPageSourceLog());
                return NemIdDetect2FAMethodsResult.canOnlyUseDefaultMethod(defaultScreen);
            }
            return NemIdDetect2FAMethodsResult.canToggleBetween2Methods(
                    defaultScreen, currentScreen);
        }

        if (elementFound == NEMID_SELECT_METHOD_POPUP) {
            return NemIdDetect2FAMethodsResult.canChooseMethodFromPopup(
                    defaultScreen, detectVisible2FAMethodInSelectionPopup());
        }

        throw LoginError.DEFAULT_MESSAGE.exception("Cannot find screen nor popup");
    }

    private boolean linkToChange2FAMethodExists() {
        return driverWrapper.tryFindElement(NEMID_LINK_TO_SELECT_DIFFERENT_2FA_METHOD).isPresent();
    }

    private void clickChange2FAMethodLink() {
        driverWrapper.clickButton(NEMID_LINK_TO_SELECT_DIFFERENT_2FA_METHOD);
    }

    private By searchForScreenOrPopup() {
        ElementsSearchResult searchResult =
                driverWrapper.searchForFirstElement(
                        ElementsSearchQuery.builder()
                                .searchInAnIframe(
                                        ELEMENTS_TO_SEARCH_FOR_AFTER_CLICKING_CHANGE_METHOD_LINK)
                                .build());
        return searchResult.getSelector();
    }

    private Set<NemId2FAMethod> detectVisible2FAMethodInSelectionPopup() {
        MultipleElementsSearchResult searchResults =
                driverWrapper.searchForAllElements(
                        ElementsSearchQuery.builder()
                                .searchInAnIframe(
                                        NemIdSelect2FAPopupOptionButton.getSelectorsForAllButtons())
                                .build());
        return searchResults.getElementsSearchResults().stream()
                .filter(searchResult -> searchResult.getWebElement().isDisplayed())
                .map(
                        searchResult ->
                                NemIdSelect2FAPopupOptionButton.getBySelector(
                                        searchResult.getSelector()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(NemIdSelect2FAPopupOptionButton::getScreenThatButtonSwitchesTo)
                .map(NemId2FAMethodScreen::getSupportedMethod)
                .collect(Collectors.toSet());
    }
}
