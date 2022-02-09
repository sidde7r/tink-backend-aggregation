package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.steps;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.MitIdConstants.MIT_ID_LOG_TAG;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.MitIdLocator.LOC_CPR_BUTTON_OK;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.MitIdLocator.LOC_CPR_INPUT;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.MitIdLocatorsElements;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.fields.MitIdCprField;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.integration.webdriver.service.WebDriverService;
import se.tink.libraries.i18n_aggregation.Catalog;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class MitIdEnterCprStep {

    private final SupplementalInformationController supplementalInformationController;
    private final Catalog catalog;

    private final WebDriverService driverService;
    private final MitIdLocatorsElements locatorsElements;

    public void enterCpr() {
        log.info("{} Entering CPR", MIT_ID_LOG_TAG);
        String cpr = askUserForValidCpr();
        enterCpr(cpr);
    }

    public void enterCpr(String cpr) {
        setCprInput(cpr);
        driverService.clickButton(locatorsElements.getElementLocator(LOC_CPR_BUTTON_OK));
    }

    private String askUserForValidCpr() {
        log.info("{} Asking user for CPR", MIT_ID_LOG_TAG);
        Field askCprField = MitIdCprField.build(catalog);

        String cpr =
                supplementalInformationController
                        .askSupplementalInformationSync(askCprField)
                        .get(askCprField.getName());

        MitIdCprField.assertValidCpr(cpr);
        return cpr;
    }

    public void setCprInput(String cpr) {
        driverService.setValueToElement(cpr, locatorsElements.getElementLocator(LOC_CPR_INPUT));
    }
}
