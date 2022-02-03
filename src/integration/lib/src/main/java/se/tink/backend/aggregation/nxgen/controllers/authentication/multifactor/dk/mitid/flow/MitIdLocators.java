package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.MitIdLocator.LOC_CHANGE_AUTH_METHOD_LINK;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.MitIdLocator.LOC_CHOOSE_CODE_APP_BUTTON;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.MitIdLocator.LOC_CHOOSE_CODE_CHIP_BUTTON;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.MitIdLocator.LOC_CHOOSE_CODE_DISPLAY_BUTTON;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.MitIdLocator.LOC_CHOOSE_METHOD_TITLE;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.MitIdLocator.LOC_CODE_APP_SCREEN_TITLE;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.MitIdLocator.LOC_CONTINUE_BUTTON;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.MitIdLocator.LOC_CPR_BUTTON_OK;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.MitIdLocator.LOC_CPR_INPUT;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.MitIdLocator.LOC_ENTER_PASSWORD_INPUT;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.MitIdLocator.LOC_ERROR_NOTIFICATION;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.MitIdLocator.LOC_TRY_AGAIN_BUTTON;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.MitIdLocator.LOC_USERNAME_INPUT;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import se.tink.integration.webdriver.service.searchelements.ElementFilter;
import se.tink.integration.webdriver.service.searchelements.ElementLocator;

public class MitIdLocators {

    private static final String TAG_LINK = "a";
    private static final String TAG_INPUT = "input";
    private static final String TAG_BUTTON = "button";
    private static final String TAG_LABEL = "label";

    private static final String ATTR_ARIA_LABEL = "aria-label";

    private static final List<String> ATTRIBUTES_THAT_MIGHT_CONTAIN_CPR =
            asList("id", "class", "name", "placeholder", ATTR_ARIA_LABEL);

    private Map<MitIdLocator, ElementLocator> locators = new HashMap<>();

    public ElementLocator getElementLocator(MitIdLocator mitIdLocator) {
        return locators.get(mitIdLocator);
    }

    public MitIdLocator getMitIdLocatorByElementLocator(ElementLocator locator) {

        return locators.entrySet().stream()
                .filter(entry -> entry.getValue() == locator)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Cannot find MitID locator by element locator: "
                                                + locator));
    }

    public void applyModifier(
            BiFunction<MitIdLocator, ElementLocator, ElementLocator> locatorFunction) {
        locators =
                locators.entrySet().stream()
                        .collect(
                                Collectors.toMap(
                                        Map.Entry::getKey,
                                        entry ->
                                                locatorFunction.apply(
                                                        entry.getKey(), entry.getValue())));
    }

    @SuppressWarnings("squid:S138")
    public MitIdLocators() {
        /*
        Login screen
        */
        locators.put(
                LOC_USERNAME_INPUT,
                ElementLocator.builder()
                        .element(By.tagName(TAG_INPUT))
                        .mustBeVisible()
                        .mustHaveAttributeContainAnyTextLowerCase(
                                ATTR_ARIA_LABEL, "bruger-id", "user id")
                        .build());

        locators.put(
                LOC_CONTINUE_BUTTON,
                ElementLocator.builder()
                        .element(By.tagName(TAG_BUTTON))
                        .mustBeVisible()
                        .mustContainAnyTextIgnoreCase("continue", "fortsæt")
                        .build());

        /*
        Common 2FA screen elements
        */
        locators.put(
                LOC_CHANGE_AUTH_METHOD_LINK,
                ElementLocator.builder()
                        .element(By.tagName(TAG_LINK))
                        .mustBeVisible()
                        .mustContainAnyTextIgnoreCase(
                                "Switch MitID authenticator", "Skift MitID identifikationsmiddel")
                        .build());

        /*
        MitID app screen
         */
        locators.put(
                LOC_CODE_APP_SCREEN_TITLE,
                ElementLocator.builder()
                        .element(By.tagName(TAG_LABEL))
                        .mustBeVisible()
                        .mustContainAnyTextIgnoreCase(
                                "Open MitID app and approve", "Åbn MitID app og godkend")
                        .build());

        /*
        2FA method selector
         */
        locators.put(
                LOC_CHOOSE_METHOD_TITLE,
                ElementLocator.builder()
                        .element(By.tagName(TAG_LABEL))
                        .mustContainAnyTextIgnoreCase(
                                "Select authenticator", "Vælg identifikationsmiddel")
                        .build());
        locators.put(
                LOC_CHOOSE_CODE_APP_BUTTON,
                ElementLocator.builder()
                        .element(By.tagName(TAG_BUTTON))
                        .mustBeVisible()
                        .mustContainAnyTextIgnoreCase("MitID app")
                        .build());
        locators.put(
                LOC_CHOOSE_CODE_DISPLAY_BUTTON,
                ElementLocator.builder()
                        .element(By.tagName(TAG_BUTTON))
                        .mustBeVisible()
                        .mustContainAnyTextIgnoreCase("MitID code display", "MitID kodeviser")
                        .build());
        locators.put(
                LOC_CHOOSE_CODE_CHIP_BUTTON,
                ElementLocator.builder()
                        .element(By.tagName(TAG_BUTTON))
                        .mustBeVisible()
                        .mustContainAnyTextIgnoreCase("MitID chip")
                        .build());

        /*
        Enter password screen
         */
        locators.put(
                LOC_ENTER_PASSWORD_INPUT,
                ElementLocator.builder()
                        .topmostIframe(By.tagName("iframe"))
                        .element(By.tagName(TAG_INPUT))
                        .mustHaveAttributeContainAnyTextLowerCase(
                                ATTR_ARIA_LABEL, "Enter your password", "Indtast din adgangskode")
                        .mustBeVisible()
                        .build());

        /*
        Error screen
         */
        locators.put(
                LOC_TRY_AGAIN_BUTTON,
                ElementLocator.builder()
                        .element(By.tagName(TAG_BUTTON))
                        .mustContainAnyTextIgnoreCase("Try again", "Prøv igen")
                        .mustBeVisible()
                        .build());
        locators.put(
                LOC_ERROR_NOTIFICATION,
                ElementLocator.builder()
                        .element(By.xpath("//*[@role='alert']"))
                        .mustBeVisible()
                        .mustHaveTextMatching(StringUtils::isNotBlank)
                        .build());

        /*
        CPR screen
         */
        locators.put(
                LOC_CPR_INPUT,
                ElementLocator.builder()
                        .element(By.tagName(TAG_INPUT))
                        .mustComplyWithFilter(isCprRelatedElement())
                        .mustBeVisible()
                        .build());
        locators.put(
                LOC_CPR_BUTTON_OK,
                ElementLocator.builder()
                        .element(By.tagName(TAG_BUTTON))
                        .mustBeVisible()
                        .mustComplyWithFilter(isCprRelatedElement())
                        .mustContainAnyTextIgnoreCase("ok")
                        .build());
    }

    private ElementFilter isCprRelatedElement() {
        return (element, driverWrapper, basicUtils) ->
                ATTRIBUTES_THAT_MIGHT_CONTAIN_CPR.stream()
                        .anyMatch(
                                attributeName -> {
                                    String attributeValue = element.getAttribute(attributeName);
                                    return containsIgnoreCase(attributeValue, "cpr");
                                });
    }
}
