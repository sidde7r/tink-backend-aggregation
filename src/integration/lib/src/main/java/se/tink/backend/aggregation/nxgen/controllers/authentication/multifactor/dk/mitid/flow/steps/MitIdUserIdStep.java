package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.steps;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.MitIdConstants.MIT_ID_LOG_TAG;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.MitIdLocator.LOC_USERNAME_INPUT;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.MitIdLocators;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.fields.MitIdUserIdField;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.integration.webdriver.service.WebDriverService;
import se.tink.libraries.i18n.Catalog;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class MitIdUserIdStep {

    private final Catalog catalog;
    private final SupplementalInformationController supplementalInformationController;

    private final WebDriverService driverService;
    private final MitIdLocators locators;
    private final MitIdCommonStepUtils commonStepUtils;

    public void enterUserId() {
        log.info("{} Entering user id", MIT_ID_LOG_TAG);
        String userId = askUserForValidUserId();
        setUserIdInput(userId);
        commonStepUtils.clickContinue();
    }

    private String askUserForValidUserId() {
        Field askUserIdField = MitIdUserIdField.build(catalog);

        String userId =
                supplementalInformationController
                        .askSupplementalInformationSync(askUserIdField)
                        .get(askUserIdField.getName());

        MitIdUserIdField.assertValidUserId(userId);
        return userId;
    }

    public void setUserIdInput(String username) {
        driverService.setValueToElement(username, locators.getElementLocator(LOC_USERNAME_INPUT));
    }
}
