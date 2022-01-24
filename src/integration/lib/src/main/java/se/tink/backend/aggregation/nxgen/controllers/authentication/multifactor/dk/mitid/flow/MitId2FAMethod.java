package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.MitIdLocator.LOC_CHOOSE_CODE_APP_BUTTON;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.MitIdLocator.LOC_CHOOSE_CODE_CHIP_BUTTON;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.MitIdLocator.LOC_CHOOSE_CODE_DISPLAY_BUTTON;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import se.tink.integration.webdriver.service.searchelements.ElementLocator;

@Getter
@RequiredArgsConstructor
public enum MitId2FAMethod {
    CODE_APP_METHOD(LOC_CHOOSE_CODE_APP_BUTTON),
    CODE_DISPLAY_METHOD(LOC_CHOOSE_CODE_DISPLAY_BUTTON),
    CODE_CHIP_METHOD(LOC_CHOOSE_CODE_CHIP_BUTTON);

    private final MitIdLocator locatorToChooseMethodOnSelectorScreen;

    public static List<ElementLocator> getAllLocators(MitIdLocators locators) {
        return Stream.of(MitId2FAMethod.values())
                .map(MitId2FAMethod::getLocatorToChooseMethodOnSelectorScreen)
                .map(locators::getElementLocator)
                .collect(Collectors.toList());
    }

    public static Optional<MitId2FAMethod> getByMitIdLocator(MitIdLocator locator) {
        return Stream.of(MitId2FAMethod.values())
                .filter(method -> method.getLocatorToChooseMethodOnSelectorScreen().equals(locator))
                .findFirst();
    }
}
