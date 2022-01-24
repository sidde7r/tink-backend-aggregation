package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.steps;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.MitIdLocator.LOC_CONTINUE_BUTTON;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.MitIdLocators;
import se.tink.integration.webdriver.service.WebDriverService;

@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class MitIdCommonStepUtils {

    private final WebDriverService driverService;
    private final MitIdLocators locators;

    public void clickContinue() {
        driverService.clickButton(locators.getElementLocator(LOC_CONTINUE_BUTTON));
    }
}
