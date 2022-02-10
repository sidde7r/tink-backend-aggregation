package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.steps;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.BANK_ID_LOG_PREFIX;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.HtmlLocators.LOC_CHOOSE_2FA_METHOD_OPTION_BUTTON_LABEL;

import com.google.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreen;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreensManager;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreensQuery;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.integration.webdriver.service.WebDriverService;
import se.tink.integration.webdriver.service.searchelements.ElementsSearchQuery;
import se.tink.libraries.i18n_aggregation.Catalog;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class BankIdAskUserToChoose2FAMethodNameStep {

    private final WebDriverService webDriver;
    private final BankIdScreensManager screensManager;

    private final Catalog catalog;
    private final SupplementalInformationController supplementalInformationController;

    public String choose2FAMethodName() {
        waitForChoose2FAMethodScreen();

        List<String> optionButtonLabels = getAll2FAOptionButtonLabels();
        log.info("{} Available 2FA button labels: {}", BANK_ID_LOG_PREFIX, optionButtonLabels);

        String labelChosenByUser = askUserToChooseMethod(optionButtonLabels);
        log.info("{} User chose label: {}", BANK_ID_LOG_PREFIX, labelChosenByUser);

        return labelChosenByUser;
    }

    private void waitForChoose2FAMethodScreen() {
        log.info("{} Searching for choose 2FA screen", BANK_ID_LOG_PREFIX);
        screensManager.waitForAnyScreenFromQuery(
                BankIdScreensQuery.builder()
                        .waitForScreens(BankIdScreen.CHOOSE_2FA_METHOD_SCREEN)
                        .verifyNoErrorScreens(true)
                        .build());
    }

    private List<String> getAll2FAOptionButtonLabels() {
        List<String> buttonLabels =
                webDriver
                        .searchForFirstMatchingLocator(
                                ElementsSearchQuery.builder()
                                        .searchFor(LOC_CHOOSE_2FA_METHOD_OPTION_BUTTON_LABEL)
                                        .searchForSeconds(10)
                                        .build())
                        .getWebElementsFound().stream()
                        .map(element -> element.getAttribute("textContent"))
                        .collect(Collectors.toList());
        if (buttonLabels.isEmpty()) {
            throw new IllegalStateException("No 2FA method option button labels found");
        }
        return buttonLabels;
    }

    private String askUserToChooseMethod(List<String> optionLabels) {
        log.info("{} Asking user to choose 2FA method label", BANK_ID_LOG_PREFIX);

        Field choose2FAMethodLabelField = BankIdChoose2FAMethodField.build(catalog, optionLabels);

        Map<String, String> supplementalInfoResponse =
                supplementalInformationController.askSupplementalInformationSync(
                        choose2FAMethodLabelField);

        return supplementalInfoResponse.get(choose2FAMethodLabelField.getName());
    }
}
