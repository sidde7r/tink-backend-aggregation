package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements;

import java.nio.file.Paths;
import org.junit.Ignore;
import org.openqa.selenium.By;

@Ignore
public class ExamplePageData {

    private static final String TEST_DATA_DIR =
            "src/integration/lib/src/test/java/se/tink/backend/aggregation/nxgen/controllers/authentication/multifactor/no/nextbankid/driver/searchelements/resources/";
    static final String EXAMPLE_HTML_PAGE_URL =
            Paths.get(TEST_DATA_DIR, "example_page.html").toUri().toString();

    /*
    Css selectors for HTML elements "containers" - iFrames & shadow DOM hosts.
    The same class name is purposefully assigned to more than 1 HTML element to check that we will find the correct one.
     */
    static final By BY_SHADOW_HOST_1 = By.cssSelector(".shadowHost1");
    static final By BY_SHADOW_HOST_2 = By.cssSelector(".shadowHost2");
    static final By BY_SHADOW_HOST_WITHOUT_ROOT = By.cssSelector(".shadowHostWithoutRoot");
    static final By BY_NOT_EXISTING_SHADOW_HOST = By.cssSelector(".notExistingHost1234");

    static final By BY_IFRAME = By.cssSelector(".iframe");
    static final By BY_NOT_EXISTING_IFRAME = By.cssSelector(".notExistingIframe1234");

    /*
    Css selectors for all elements that we will be looking for in the mocked HTML page.
    The same class name is purposefully assigned to more than 1 HTML element to check if we can find the correct one
    using other localization parts like correct iframe and/or shadow host.
     */
    static final By.ByCssSelector BY_MAIN_ELEMENT = new By.ByCssSelector(".mainElementSelector");
    static final By.ByCssSelector BY_OTHER_ELEMENT = new By.ByCssSelector(".otherElementSelector");
    static final By.ByCssSelector BY_NOT_EXISTING_ELEMENT =
            new By.ByCssSelector(".notExistingElement1234");

    /*
    Elements ids
     */
    static final String PARENT_MAIN_ELEMENT_VISIBLE = "PARENT_MAIN_ELEMENT_VISIBLE";
    static final String PARENT_MAIN_ELEMENT_HIDDEN = "PARENT_MAIN_ELEMENT_HIDDEN";
    static final String PARENT_OTHER_ELEMENT = "PARENT_OTHER_ELEMENT";

    static final String PARENT_SHADOW_HOST_1_MAIN_ELEMENT_VISIBLE =
            "PARENT_SHADOW_HOST_1_MAIN_ELEMENT_VISIBLE";
    static final String PARENT_SHADOW_HOST_1_MAIN_ELEMENT_HIDDEN =
            "PARENT_SHADOW_HOST_1_MAIN_ELEMENT_HIDDEN";
    static final String PARENT_SHADOW_HOST_1_OTHER_ELEMENT = "PARENT_SHADOW_HOST_1_OTHER_ELEMENT";

    static final String PARENT_SHADOW_HOST_2_MAIN_ELEMENT_VISIBLE =
            "PARENT_SHADOW_HOST_2_MAIN_ELEMENT_VISIBLE";
    static final String PARENT_SHADOW_HOST_2_MAIN_ELEMENT_HIDDEN =
            "PARENT_SHADOW_HOST_2_MAIN_ELEMENT_HIDDEN";
    static final String PARENT_SHADOW_HOST_2_OTHER_ELEMENT = "PARENT_SHADOW_HOST_2_OTHER_ELEMENT";

    static final String IFRAME_MAIN_ELEMENT_VISIBLE = "IFRAME_MAIN_ELEMENT_VISIBLE";
    static final String IFRAME_MAIN_ELEMENT_HIDDEN = "IFRAME_MAIN_ELEMENT_HIDDEN";
    static final String IFRAME_OTHER_ELEMENT = "IFRAME_OTHER_ELEMENT";

    static final String IFRAME_SHADOW_HOST_1_MAIN_ELEMENT_VISIBLE =
            "IFRAME_SHADOW_HOST_1_MAIN_ELEMENT_VISIBLE";
    static final String IFRAME_SHADOW_HOST_1_MAIN_ELEMENT_HIDDEN =
            "IFRAME_SHADOW_HOST_1_MAIN_ELEMENT_HIDDEN";
    static final String IFRAME_SHADOW_HOST_1_OTHER_ELEMENT = "IFRAME_SHADOW_HOST_1_OTHER_ELEMENT";

    static final String IFRAME_SHADOW_HOST_2_MAIN_ELEMENT_VISIBLE =
            "IFRAME_SHADOW_HOST_2_MAIN_ELEMENT_VISIBLE";
    static final String IFRAME_SHADOW_HOST_2_MAIN_ELEMENT_HIDDEN =
            "IFRAME_SHADOW_HOST_2_MAIN_ELEMENT_HIDDEN";
    static final String IFRAME_SHADOW_HOST_2_OTHER_ELEMENT = "IFRAME_SHADOW_HOST_2_OTHER_ELEMENT";
}
