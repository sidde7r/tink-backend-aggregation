package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.choosemethod;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.NEMID_LINK_TO_SELECT_DIFFERENT_2FA_METHOD;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.NEMID_SELECT_METHOD_POPUP;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.NEM_ID_PREFIX;

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
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.utils.NemIdWebDriverWrapper;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
class NemIdDetect2FAMethodsStep {

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
            return handleNoLinkToChangeMethod(defaultScreen);
        }

        clickChange2FAMethodLink();
        return detect2FAMethodsByTheEffectOfClickingLink(defaultScreen);
    }

    /*
     * Depending on how many 2FA methods user has, the change method link behaves differently:
     * - for 2 methods it toggles between the only 2 possible 2FA screens
     * - for 3 methods (or possibly more) it doesn't change screen but opens a popup instead -
     *   this popup allows to choose and switch to the screen dedicated for one of the 2 remaining
     *   methods
     */
    private NemIdDetect2FAMethodsResult detect2FAMethodsByTheEffectOfClickingLink(
            NemId2FAMethodScreen defaultScreen) {

        // wait a second to give screens some time to reload
        driverWrapper.sleepFor(1_000);
        By elementFound = searchForScreenOrPopup();

        Optional<NemId2FAMethodScreen> maybeMethodScreen =
                NemId2FAMethodScreen.getScreenBySelector(elementFound);
        if (maybeMethodScreen.isPresent()) {
            log.info(
                    "{}[NemIdDetect2FAMethodsStep] Detected 2FA method {}",
                    NEM_ID_PREFIX,
                    maybeMethodScreen.get().getSupportedMethod().getUserFriendlyName().get());
            return handleSomeScreenIsVisibleAfterClickingLink(
                    defaultScreen, maybeMethodScreen.get());
        }

        if (elementFound == NEMID_SELECT_METHOD_POPUP) {
            return NemIdDetect2FAMethodsResult.canChooseMethodFromPopup(
                    defaultScreen, detectVisible2FAMethodInSelectionPopup());
        }

        throw LoginError.DEFAULT_MESSAGE.exception("Cannot find screen nor popup");
    }

    private NemIdDetect2FAMethodsResult handleNoLinkToChangeMethod(
            NemId2FAMethodScreen defaultScreen) {
        // It's a guess that if user has only 1 NemID method available there might not be any
        // link to change it
        log.info(
                "{}[NemIdDetect2FAMethodsStep] Cannot find link to change 2FA method. Check page source:\n{}",
                NEM_ID_PREFIX,
                driverWrapper.getFullPageSourceLog());
        return NemIdDetect2FAMethodsResult.canOnlyUseDefaultMethod(defaultScreen);
    }

    private NemIdDetect2FAMethodsResult handleSomeScreenIsVisibleAfterClickingLink(
            NemId2FAMethodScreen defaultScreen, NemId2FAMethodScreen currentlyVisibleScreen) {
        if (currentlyVisibleScreen == defaultScreen) {
            // It's a guess that if user has only 1 NemID method available there might be a link
            // but it doesn't change it
            log.info(
                    "{}[NemIdDetect2FAMethodsStep] Link to change 2FA method does not work. Check page source:\n{}",
                    NEM_ID_PREFIX,
                    driverWrapper.getFullPageSourceLog());
            return NemIdDetect2FAMethodsResult.canOnlyUseDefaultMethod(defaultScreen);
        }

        return NemIdDetect2FAMethodsResult.canToggleBetween2Methods(
                defaultScreen, currentlyVisibleScreen);
    }

    private boolean linkToChange2FAMethodExists() {
        return driverWrapper.tryFindElement(NEMID_LINK_TO_SELECT_DIFFERENT_2FA_METHOD).isPresent();
    }

    private void clickChange2FAMethodLink() {
        log.info("{}[NemIdDetect2FAMethodsStep] Changing 2FA step", NEM_ID_PREFIX);
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
        List<ElementsSearchResult> searchResults =
                driverWrapper.searchForAllElements(
                        ElementsSearchQuery.builder()
                                .searchInAnIframe(
                                        NemIdSelect2FAPopupOptionButton.getSelectorsForAllButtons())
                                .build());
        return searchResults.stream()
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
