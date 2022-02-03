package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import se.tink.integration.webdriver.service.searchelements.ElementFilter;
import se.tink.integration.webdriver.service.searchelements.ElementLocator;

@Getter
@RequiredArgsConstructor
public enum MitIdLocator {
    /*
    Login screen
    */
    LOC_USERNAME_INPUT(
            ElementLocator.builder()
                    .element(By.tagName(Constants.TAG_INPUT))
                    .mustBeVisible()
                    .mustHaveAttributeContainAnyTextLowerCase(
                            Constants.ATTR_ARIA_LABEL, "bruger-id", "user id")
                    .build()),

    LOC_CONTINUE_BUTTON(
            ElementLocator.builder()
                    .element(By.tagName(Constants.TAG_BUTTON))
                    .mustBeVisible()
                    .mustContainAnyTextIgnoreCase("continue", "fortsæt")
                    .build()),

    /*
    Common 2FA screen elements
    */
    LOC_CHANGE_AUTH_METHOD_LINK(
            ElementLocator.builder()
                    .element(By.tagName(Constants.TAG_LINK))
                    .mustBeVisible()
                    .mustContainAnyTextIgnoreCase(
                            "Switch MitID authenticator", "Skift MitID identifikationsmiddel")
                    .build()),

    /*
    MitID app screen
     */
    LOC_CODE_APP_SCREEN_TITLE(
            ElementLocator.builder()
                    .element(By.tagName(Constants.TAG_LABEL))
                    .mustBeVisible()
                    .mustContainAnyTextIgnoreCase(
                            "Open MitID app and approve", "Åbn MitID app og godkend")
                    .build()),

    /*
    2FA method selector
     */
    LOC_CHOOSE_METHOD_TITLE(
            ElementLocator.builder()
                    .element(By.tagName(Constants.TAG_LABEL))
                    .mustContainAnyTextIgnoreCase(
                            "Select authenticator", "Vælg identifikationsmiddel")
                    .build()),
    LOC_CHOOSE_CODE_APP_BUTTON(
            ElementLocator.builder()
                    .element(By.tagName(Constants.TAG_BUTTON))
                    .mustBeVisible()
                    .mustContainAnyTextIgnoreCase("MitID app")
                    .build()),
    LOC_CHOOSE_CODE_DISPLAY_BUTTON(
            ElementLocator.builder()
                    .element(By.tagName(Constants.TAG_BUTTON))
                    .mustBeVisible()
                    .mustContainAnyTextIgnoreCase("MitID code display", "MitID kodeviser")
                    .build()),
    LOC_CHOOSE_CODE_CHIP_BUTTON(
            ElementLocator.builder()
                    .element(By.tagName(Constants.TAG_BUTTON))
                    .mustBeVisible()
                    .mustContainAnyTextIgnoreCase("MitID chip")
                    .build()),

    /*
    Enter password screen
     */
    LOC_ENTER_PASSWORD_INPUT(
            ElementLocator.builder()
                    .topmostIframe(By.tagName("iframe"))
                    .element(By.tagName(Constants.TAG_INPUT))
                    .mustHaveAttributeContainAnyTextLowerCase(
                            Constants.ATTR_ARIA_LABEL,
                            "Enter your password",
                            "Indtast din adgangskode")
                    .mustBeVisible()
                    .build()),

    /*
    Error screen
     */
    LOC_TRY_AGAIN_BUTTON(
            ElementLocator.builder()
                    .element(By.tagName(Constants.TAG_BUTTON))
                    .mustContainAnyTextIgnoreCase("Try again", "Prøv igen")
                    .mustBeVisible()
                    .build()),
    LOC_ERROR_NOTIFICATION(
            ElementLocator.builder()
                    .element(By.xpath("//*[@role='alert']"))
                    .mustBeVisible()
                    .mustHaveTextMatching(StringUtils::isNotBlank)
                    .build()),

    /*
    CPR screen
     */
    LOC_CPR_INPUT(
            ElementLocator.builder()
                    .element(By.tagName(Constants.TAG_INPUT))
                    .mustComplyWithFilter(isCprRelatedElement())
                    .mustBeVisible()
                    .build()),
    LOC_CPR_BUTTON_OK(
            ElementLocator.builder()
                    .element(By.tagName(Constants.TAG_BUTTON))
                    .mustBeVisible()
                    .mustComplyWithFilter(isCprRelatedElement())
                    .mustContainAnyTextIgnoreCase("ok")
                    .build());

    private final ElementLocator defaultElementLocator;

    private static ElementFilter isCprRelatedElement() {
        return (element, driverWrapper, basicUtils) ->
                Constants.ATTRIBUTES_THAT_MIGHT_CONTAIN_CPR.stream()
                        .anyMatch(
                                attributeName -> {
                                    String attributeValue = element.getAttribute(attributeName);
                                    return containsIgnoreCase(attributeValue, "cpr");
                                });
    }

    private static class Constants {
        private static final String TAG_LINK = "a";
        private static final String TAG_INPUT = "input";
        private static final String TAG_BUTTON = "button";
        private static final String TAG_LABEL = "label";

        private static final String ATTR_ARIA_LABEL = "aria-label";

        private static final List<String> ATTRIBUTES_THAT_MIGHT_CONTAIN_CPR =
                asList("id", "class", "name", "placeholder", ATTR_ARIA_LABEL);
    }
}
