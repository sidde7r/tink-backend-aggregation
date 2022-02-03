package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.steps;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.MitIdConstants.MIT_ID_LOG_TAG;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.MitIdConstants.WaitTime.WAIT_FOR_ANY_SELECT_METHOD_BUTTON;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.MitIdConstants.WaitTime.WAIT_FOR_METHOD_SELECTOR_SCREEN;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.MitIdConstants.WaitTime.WAIT_TO_CHECK_IF_THERE_IS_CHANGE_METHOD_LINK;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.MitIdConstants.WaitTime.WAIT_TO_DETECT_2FA_SCREEN;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.MitIdLocator.LOC_CHANGE_AUTH_METHOD_LINK;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.MitIdLocator.LOC_CONTINUE_BUTTON;

import com.google.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.mitid.MitIdError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.MitId2FAMethod;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.MitIdLocator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.MitIdLocators;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.screens.MitIdScreen;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.screens.MitIdScreenQuery;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.screens.MitIdScreensManager;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.steps.codeapp.MitIdCodeAppStep;
import se.tink.integration.webdriver.service.WebDriverService;
import se.tink.integration.webdriver.service.searchelements.ElementLocator;
import se.tink.integration.webdriver.service.searchelements.ElementsSearchQuery;
import se.tink.integration.webdriver.service.searchelements.ElementsSearchResult;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class MitId2FAStep {

    private final WebDriverService driverService;
    private final MitIdLocators locators;
    private final MitIdScreensManager screensManager;
    private final MitIdCodeAppStep codeAppStep;

    public void perform2FA() {
        log.info("{} Performing 2FA", MIT_ID_LOG_TAG);
        MitIdScreen secondFactorScreen = find2FAScreen();

        switch (secondFactorScreen) {
            case CODE_APP_SCREEN:
                continueWithCodeAppAuthentication();
                break;
                /*
                Enter password screen means that the 2FA method is either Code Display or Code Chip.
                Currently, we only support MitID app as it's the most popular method. This is why we
                "manually" switch to Code App method without asking user about it.
                The next goal is to add support for Code Display [ITE-3274]
                 */
            case ENTER_PASSWORD_SCREEN:
                switchToCodeAppAuthentication();
                continueWithCodeAppAuthentication();
                break;
            default:
                throw new IllegalStateException("Unexpected 2FA screen: " + secondFactorScreen);
        }
    }

    private MitIdScreen find2FAScreen() {
        return screensManager.searchForFirstScreen(
                MitIdScreenQuery.builder()
                        .searchForExpectedScreens(MitIdScreen.SECOND_FACTOR_SCREENS)
                        .searchForSeconds(WAIT_TO_DETECT_2FA_SCREEN)
                        .build());
    }

    private void continueWithCodeAppAuthentication() {
        codeAppStep.authenticateWithCodeApp();
    }

    private void switchToCodeAppAuthentication() {
        boolean linkToChangeMethodExists = hasLinkToChangeMethod();
        if (!linkToChangeMethodExists) {
            throw MitIdError.ONLY_CODE_APP_METHOD_SUPPORTED.exception(
                    "No link to change authentication method");
        }

        clickLinkToChangeMethod();
        assertIsOnMethodSelectorScreen();

        List<MitId2FAMethod> available2FAMethods = getAvailable2FAMethods();
        log.info("{} Available 2FA methods: {}", MIT_ID_LOG_TAG, available2FAMethods);
        if (!available2FAMethods.contains(MitId2FAMethod.CODE_APP_METHOD)) {
            throw MitIdError.ONLY_CODE_APP_METHOD_SUPPORTED.exception(
                    "Cannot select code app method");
        }

        selectMethod(MitId2FAMethod.CODE_APP_METHOD);
        driverService.clickButton(locators.getElementLocator(LOC_CONTINUE_BUTTON));
    }

    public boolean hasLinkToChangeMethod() {
        return driverService
                .searchForFirstMatchingLocator(
                        ElementsSearchQuery.builder()
                                .searchFor(locators.getElementLocator(LOC_CHANGE_AUTH_METHOD_LINK))
                                .searchForSeconds(WAIT_TO_CHECK_IF_THERE_IS_CHANGE_METHOD_LINK)
                                .build())
                .isNotEmpty();
    }

    public void assertIsOnMethodSelectorScreen() {
        screensManager.searchForFirstScreen(
                MitIdScreenQuery.builder()
                        .searchForExpectedScreens(MitIdScreen.METHOD_SELECTOR_SCREEN)
                        .searchForSeconds(WAIT_FOR_METHOD_SELECTOR_SCREEN)
                        .build());
    }

    public void clickLinkToChangeMethod() {
        driverService.clickButton(locators.getElementLocator(LOC_CHANGE_AUTH_METHOD_LINK));
    }

    public List<MitId2FAMethod> getAvailable2FAMethods() {
        List<ElementsSearchResult> searchResults =
                driverService.searchForAllMatchingLocators(
                        ElementsSearchQuery.builder()
                                .searchFor(MitId2FAMethod.getAllLocators(locators))
                                .searchForSeconds(WAIT_FOR_ANY_SELECT_METHOD_BUTTON)
                                .build());
        return searchResults.stream()
                .map(
                        result -> {
                            ElementLocator locator = result.getLocatorFound();
                            MitIdLocator mitIdLocator =
                                    locators.getMitIdLocatorByElementLocator(locator);
                            return MitId2FAMethod.getByMitIdLocator(mitIdLocator)
                                    .orElseThrow(
                                            () ->
                                                    new IllegalStateException(
                                                            "There is no 2FA method for locator: "
                                                                    + locator));
                        })
                .collect(Collectors.toList());
    }

    public void selectMethod(MitId2FAMethod method) {
        driverService.clickButton(
                locators.getElementLocator(method.getLocatorToChooseMethodOnSelectorScreen()));
    }
}
