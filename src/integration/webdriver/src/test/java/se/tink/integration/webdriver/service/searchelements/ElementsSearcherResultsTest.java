package se.tink.integration.webdriver.service.searchelements;

import static org.assertj.core.api.Assertions.assertThat;
import static se.tink.integration.webdriver.service.searchelements.ExamplePageData.BY_IFRAME;
import static se.tink.integration.webdriver.service.searchelements.ExamplePageData.BY_MAIN_ELEMENT;
import static se.tink.integration.webdriver.service.searchelements.ExamplePageData.BY_NOT_EXISTING_ELEMENT;
import static se.tink.integration.webdriver.service.searchelements.ExamplePageData.BY_NOT_EXISTING_IFRAME;
import static se.tink.integration.webdriver.service.searchelements.ExamplePageData.BY_NOT_EXISTING_SHADOW_HOST;
import static se.tink.integration.webdriver.service.searchelements.ExamplePageData.BY_OTHER_ELEMENT;
import static se.tink.integration.webdriver.service.searchelements.ExamplePageData.BY_SHADOW_HOST_1;
import static se.tink.integration.webdriver.service.searchelements.ExamplePageData.BY_SHADOW_HOST_2;
import static se.tink.integration.webdriver.service.searchelements.ExamplePageData.BY_SHADOW_HOST_WITHOUT_ROOT;
import static se.tink.integration.webdriver.service.searchelements.ExamplePageData.EXAMPLE_HTML_PAGE_URL;
import static se.tink.integration.webdriver.service.searchelements.ExamplePageData.IFRAME_MAIN_ELEMENT_HIDDEN;
import static se.tink.integration.webdriver.service.searchelements.ExamplePageData.IFRAME_MAIN_ELEMENT_VISIBLE;
import static se.tink.integration.webdriver.service.searchelements.ExamplePageData.IFRAME_OTHER_ELEMENT;
import static se.tink.integration.webdriver.service.searchelements.ExamplePageData.IFRAME_SHADOW_HOST_1_MAIN_ELEMENT_HIDDEN;
import static se.tink.integration.webdriver.service.searchelements.ExamplePageData.IFRAME_SHADOW_HOST_1_MAIN_ELEMENT_VISIBLE;
import static se.tink.integration.webdriver.service.searchelements.ExamplePageData.IFRAME_SHADOW_HOST_1_OTHER_ELEMENT;
import static se.tink.integration.webdriver.service.searchelements.ExamplePageData.IFRAME_SHADOW_HOST_2_MAIN_ELEMENT_HIDDEN;
import static se.tink.integration.webdriver.service.searchelements.ExamplePageData.IFRAME_SHADOW_HOST_2_MAIN_ELEMENT_VISIBLE;
import static se.tink.integration.webdriver.service.searchelements.ExamplePageData.IFRAME_SHADOW_HOST_2_OTHER_ELEMENT;
import static se.tink.integration.webdriver.service.searchelements.ExamplePageData.PARENT_MAIN_ELEMENT_HIDDEN;
import static se.tink.integration.webdriver.service.searchelements.ExamplePageData.PARENT_MAIN_ELEMENT_VISIBLE;
import static se.tink.integration.webdriver.service.searchelements.ExamplePageData.PARENT_OTHER_ELEMENT;
import static se.tink.integration.webdriver.service.searchelements.ExamplePageData.PARENT_SHADOW_HOST_1_MAIN_ELEMENT_HIDDEN;
import static se.tink.integration.webdriver.service.searchelements.ExamplePageData.PARENT_SHADOW_HOST_1_MAIN_ELEMENT_VISIBLE;
import static se.tink.integration.webdriver.service.searchelements.ExamplePageData.PARENT_SHADOW_HOST_1_OTHER_ELEMENT;
import static se.tink.integration.webdriver.service.searchelements.ExamplePageData.PARENT_SHADOW_HOST_2_MAIN_ELEMENT_HIDDEN;
import static se.tink.integration.webdriver.service.searchelements.ExamplePageData.PARENT_SHADOW_HOST_2_MAIN_ELEMENT_VISIBLE;
import static se.tink.integration.webdriver.service.searchelements.ExamplePageData.PARENT_SHADOW_HOST_2_OTHER_ELEMENT;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.integration.webdriver.ChromeDriverInitializer;
import se.tink.integration.webdriver.WebDriverWrapper;
import se.tink.integration.webdriver.service.basicutils.Sleeper;
import se.tink.integration.webdriver.service.basicutils.WebDriverBasicUtils;
import se.tink.integration.webdriver.service.basicutils.WebDriverBasicUtilsImpl;

/**
 * The goal of this test class is to check that {@link ElementsSearcher} will eventually find
 * correct locator and its elements.
 */
@Slf4j
@RunWith(JUnitParamsRunner.class)
public class ElementsSearcherResultsTest {

    /*
    Real
     */
    private static WebDriverWrapper driver;
    private ElementsSearcher elementsSearcher;

    @BeforeClass
    public static void setupDriver() {
        driver = ChromeDriverInitializer.constructChromeDriver();
    }

    @AfterClass
    public static void quitDriver() {
        ChromeDriverInitializer.quitChromeDriver(driver);
    }

    @Before
    public void setupTest() {
        WebDriverBasicUtils driverBasicUtils = new WebDriverBasicUtilsImpl(driver, new Sleeper());
        elementsSearcher = new ElementsSearcherImpl(driver, driverBasicUtils);

        driver.get(EXAMPLE_HTML_PAGE_URL);
    }

    @Test
    @Parameters(method = "shouldFindCorrectElementsForLocatorTestParams")
    public void should_find_elements_for_selector(
            String testParamsId, // here only to help with finding failing test params
            List<ElementLocator> locatorsToSearchFor,
            ElementLocator expectedLocatorFound,
            List<String> expectedIdsForFoundElements) {
        // when
        ElementsSearchResult searchResult =
                elementsSearcher.searchForFirstMatchingLocator(
                        ElementsSearchQuery.builder()
                                .searchFor(locatorsToSearchFor)
                                .searchOnlyOnce()
                                .build());
        // then
        boolean shouldResultBeEmpty = expectedIdsForFoundElements.isEmpty();
        assertThat(searchResult.isEmpty()).isEqualTo(shouldResultBeEmpty);

        if (shouldResultBeEmpty) {
            return;
        }

        assertThat(searchResult.getLocatorFound()).isEqualTo(expectedLocatorFound);
        assertThat(searchResult.getWebElementsFound().size())
                .isEqualTo(expectedIdsForFoundElements.size());

        for (int i = 0; i < expectedIdsForFoundElements.size(); i++) {

            String actualElementId = searchResult.getWebElementsFound().get(i).getAttribute("id");
            String expectedElementId = expectedIdsForFoundElements.get(i);

            assertThat(actualElementId).isEqualTo(expectedElementId);
        }
    }

    @SuppressWarnings("unused")
    private static Object[] shouldFindCorrectElementsForLocatorTestParams() {
        return Stream.of(
                        shouldLocalizeCorrectElements(),
                        shouldApplyElementFilters(),
                        shouldFindTheFirstLocatorWithExistingElementsIgnoringNotExistingLocators())
                .reduce(Stream::concat)
                .orElseGet(Stream::empty)
                .map(TestParams::toMethodParams)
                .toArray();
    }

    private static Stream<TestParams> shouldLocalizeCorrectElements() {
        return Stream.of(
                /*
                Parent window
                 */
                TestParams.builder("6b000fd9-ad69-4833-b419-43304ed8afac")
                        .addLocator(ElementLocator.builder().element(BY_MAIN_ELEMENT).build(), true)
                        .addIdToBeFound(PARENT_MAIN_ELEMENT_VISIBLE)
                        .addIdToBeFound(PARENT_MAIN_ELEMENT_HIDDEN)
                        .build(),
                TestParams.builder("346abc86-772b-4bc9-b942-a19db8b62b91")
                        .addLocator(
                                ElementLocator.builder().element(BY_OTHER_ELEMENT).build(), true)
                        .addIdToBeFound(PARENT_OTHER_ELEMENT)
                        .build(),
                /*
                Parent window + shadow host 1
                 */
                TestParams.builder("ced304ac-199d-4092-b7a2-f51154d20394")
                        .addLocator(
                                ElementLocator.builder()
                                        .shadowHost(BY_SHADOW_HOST_1)
                                        .element(BY_MAIN_ELEMENT)
                                        .build(),
                                true)
                        .addIdToBeFound(PARENT_SHADOW_HOST_1_MAIN_ELEMENT_VISIBLE)
                        .addIdToBeFound(PARENT_SHADOW_HOST_1_MAIN_ELEMENT_HIDDEN)
                        .build(),
                TestParams.builder("1e854e53-7c09-43fe-8122-59a7fbc6e806")
                        .addLocator(
                                ElementLocator.builder()
                                        .shadowHost(BY_SHADOW_HOST_1)
                                        .element(BY_OTHER_ELEMENT)
                                        .build(),
                                true)
                        .addIdToBeFound(PARENT_SHADOW_HOST_1_OTHER_ELEMENT)
                        .build(),
                /*
                Parent window + shadow host 2
                 */
                TestParams.builder("faa524d2-5e77-4c4d-bab2-3467da366d17")
                        .addLocator(
                                ElementLocator.builder()
                                        .shadowHost(BY_SHADOW_HOST_2)
                                        .element(BY_MAIN_ELEMENT)
                                        .build(),
                                true)
                        .addIdToBeFound(PARENT_SHADOW_HOST_2_MAIN_ELEMENT_VISIBLE)
                        .addIdToBeFound(PARENT_SHADOW_HOST_2_MAIN_ELEMENT_HIDDEN)
                        .build(),
                TestParams.builder("e6180d66-8790-4aa3-a30f-8bbe12fbd8ae")
                        .addLocator(
                                ElementLocator.builder()
                                        .shadowHost(BY_SHADOW_HOST_2)
                                        .element(BY_OTHER_ELEMENT)
                                        .build(),
                                true)
                        .addIdToBeFound(PARENT_SHADOW_HOST_2_OTHER_ELEMENT)
                        .build(),
                /*
                Iframe
                 */
                TestParams.builder("ed30020d-0998-4926-af92-4dc4e93164e6")
                        .addLocator(
                                ElementLocator.builder()
                                        .topmostIframe(BY_IFRAME)
                                        .element(BY_MAIN_ELEMENT)
                                        .build(),
                                true)
                        .addIdToBeFound(IFRAME_MAIN_ELEMENT_VISIBLE)
                        .addIdToBeFound(IFRAME_MAIN_ELEMENT_HIDDEN)
                        .build(),
                TestParams.builder("4b500b05-b9ae-46d8-ab65-d29557c3f60a")
                        .addLocator(
                                ElementLocator.builder()
                                        .topmostIframe(BY_IFRAME)
                                        .element(BY_OTHER_ELEMENT)
                                        .build(),
                                true)
                        .addIdToBeFound(IFRAME_OTHER_ELEMENT)
                        .build(),
                /*
                Iframe + shadow host 1
                 */
                TestParams.builder("bf8b1d9a-9b4d-43c9-a06a-1d7ec4a34bdc")
                        .addLocator(
                                ElementLocator.builder()
                                        .topmostIframe(BY_IFRAME)
                                        .shadowHost(BY_SHADOW_HOST_1)
                                        .element(BY_MAIN_ELEMENT)
                                        .build(),
                                true)
                        .addIdToBeFound(IFRAME_SHADOW_HOST_1_MAIN_ELEMENT_VISIBLE)
                        .addIdToBeFound(IFRAME_SHADOW_HOST_1_MAIN_ELEMENT_HIDDEN)
                        .build(),
                TestParams.builder("46108f6a-522e-4acf-8827-c18725b73250")
                        .addLocator(
                                ElementLocator.builder()
                                        .topmostIframe(BY_IFRAME)
                                        .shadowHost(BY_SHADOW_HOST_1)
                                        .element(BY_OTHER_ELEMENT)
                                        .build(),
                                true)
                        .addIdToBeFound(IFRAME_SHADOW_HOST_1_OTHER_ELEMENT)
                        .build(),
                /*
                Iframe + shadow host 2
                 */
                TestParams.builder("ea6beb3e-cd63-405b-95d7-f1e7fe4d27cf")
                        .addLocator(
                                ElementLocator.builder()
                                        .topmostIframe(BY_IFRAME)
                                        .shadowHost(BY_SHADOW_HOST_2)
                                        .element(BY_MAIN_ELEMENT)
                                        .build(),
                                true)
                        .addIdToBeFound(IFRAME_SHADOW_HOST_2_MAIN_ELEMENT_VISIBLE)
                        .addIdToBeFound(IFRAME_SHADOW_HOST_2_MAIN_ELEMENT_HIDDEN)
                        .build(),
                TestParams.builder("3930c99b-7db1-46ca-ba1e-578f7335d7d8")
                        .addLocator(
                                ElementLocator.builder()
                                        .topmostIframe(BY_IFRAME)
                                        .shadowHost(BY_SHADOW_HOST_2)
                                        .element(BY_OTHER_ELEMENT)
                                        .build(),
                                true)
                        .addIdToBeFound(IFRAME_SHADOW_HOST_2_OTHER_ELEMENT)
                        .build());
    }

    private static Stream<TestParams> shouldApplyElementFilters() {
        return Stream.of(
                /*
                Parent window
                 */
                TestParams.builder("3dd28bde-d75b-4676-a3a8-382eb2853815")
                        .addLocator(
                                ElementLocator.builder()
                                        .element(BY_MAIN_ELEMENT)
                                        .mustContainOneOfTexts("@!%^#@")
                                        .build())
                        .build(),
                TestParams.builder("d4aa6452-32f5-4d25-8abf-a494523961fe")
                        .addLocator(
                                ElementLocator.builder()
                                        .element(BY_MAIN_ELEMENT)
                                        .mustContainOneOfTexts("main element", "#@^#@^@")
                                        .build(),
                                true)
                        .addIdToBeFound(PARENT_MAIN_ELEMENT_VISIBLE)
                        .addIdToBeFound(PARENT_MAIN_ELEMENT_HIDDEN)
                        .build(),
                TestParams.builder("7fefdb05-bd56-4a9b-a773-cd87cdae8032")
                        .addLocator(
                                ElementLocator.builder()
                                        .element(BY_MAIN_ELEMENT)
                                        .mustHaveExactText("main element visible")
                                        .build(),
                                true)
                        .addIdToBeFound(PARENT_MAIN_ELEMENT_VISIBLE)
                        .build(),
                TestParams.builder("3101af7b-7986-4f19-91f2-5a67187442e4")
                        .addLocator(
                                ElementLocator.builder()
                                        .element(BY_MAIN_ELEMENT)
                                        .mustBeDisplayed()
                                        .build(),
                                true)
                        .addIdToBeFound(PARENT_MAIN_ELEMENT_VISIBLE)
                        .build(),
                TestParams.builder("2c463d30-4671-4dc3-935a-0183ef488c1d")
                        .addLocator(
                                ElementLocator.builder()
                                        .element(BY_MAIN_ELEMENT)
                                        .mustHaveExactText("main element hidden")
                                        .mustBeDisplayed()
                                        .build())
                        .build(),
                /*
                Iframe + shadow host 1
                 */
                TestParams.builder("baa916ab-b7a4-4cfa-a33a-b56fbaaae3b3")
                        .addLocator(
                                ElementLocator.builder()
                                        .topmostIframe(BY_IFRAME)
                                        .shadowHost(BY_SHADOW_HOST_1)
                                        .element(BY_MAIN_ELEMENT)
                                        .mustContainOneOfTexts("----not existing text----")
                                        .build())
                        .build(),
                TestParams.builder("31767696-def0-477e-932c-2c228573106e")
                        .addLocator(
                                ElementLocator.builder()
                                        .topmostIframe(BY_IFRAME)
                                        .shadowHost(BY_SHADOW_HOST_1)
                                        .element(BY_MAIN_ELEMENT)
                                        .mustContainOneOfTexts("element")
                                        .build(),
                                true)
                        .addIdToBeFound(IFRAME_SHADOW_HOST_1_MAIN_ELEMENT_VISIBLE)
                        .addIdToBeFound(IFRAME_SHADOW_HOST_1_MAIN_ELEMENT_HIDDEN)
                        .build(),
                TestParams.builder("bbbce985-b45a-4a1e-a0cf-7d3dc1cca64d")
                        .addLocator(
                                ElementLocator.builder()
                                        .topmostIframe(BY_IFRAME)
                                        .shadowHost(BY_SHADOW_HOST_1)
                                        .element(BY_MAIN_ELEMENT)
                                        .mustContainOneOfTexts("hidden")
                                        .build(),
                                true)
                        .addIdToBeFound(IFRAME_SHADOW_HOST_1_MAIN_ELEMENT_HIDDEN)
                        .build(),
                TestParams.builder("45616b96-779f-4da1-bb3d-0a6ec8d96339")
                        .addLocator(
                                ElementLocator.builder()
                                        .topmostIframe(BY_IFRAME)
                                        .shadowHost(BY_SHADOW_HOST_1)
                                        .element(BY_MAIN_ELEMENT)
                                        .mustBeDisplayed()
                                        .build(),
                                true)
                        .addIdToBeFound(IFRAME_SHADOW_HOST_1_MAIN_ELEMENT_VISIBLE)
                        .build(),
                TestParams.builder("5fea50cf-b091-44cf-9ba7-d06f739a242e")
                        .addLocator(
                                ElementLocator.builder()
                                        .topmostIframe(BY_IFRAME)
                                        .shadowHost(BY_SHADOW_HOST_1)
                                        .element(BY_MAIN_ELEMENT)
                                        .mustContainOneOfTexts("main element hidden")
                                        .mustBeDisplayed()
                                        .build())
                        .build());
    }

    private static Stream<TestParams>
            shouldFindTheFirstLocatorWithExistingElementsIgnoringNotExistingLocators() {
        return Stream.of(
                /*
                Parent window
                 */
                TestParams.builder("7f91f706-4813-4062-8224-4c926693af05")
                        .addLocator(
                                ElementLocator.builder().element(BY_NOT_EXISTING_ELEMENT).build())
                        .addLocator(ElementLocator.builder().element(BY_MAIN_ELEMENT).build(), true)
                        .addLocator(ElementLocator.builder().element(BY_OTHER_ELEMENT).build())
                        .addIdToBeFound(PARENT_MAIN_ELEMENT_VISIBLE)
                        .addIdToBeFound(PARENT_MAIN_ELEMENT_HIDDEN)
                        .build(),
                /*
                Parent window + shadow host 1
                 */
                TestParams.builder("21d51495-bddf-4f61-9f02-758f169bda66")
                        .addLocator(
                                ElementLocator.builder()
                                        .shadowHost(BY_NOT_EXISTING_SHADOW_HOST)
                                        .element(BY_MAIN_ELEMENT)
                                        .build())
                        .addLocator(
                                ElementLocator.builder()
                                        .shadowHost(BY_SHADOW_HOST_WITHOUT_ROOT)
                                        .element(BY_MAIN_ELEMENT)
                                        .build())
                        .addLocator(
                                ElementLocator.builder()
                                        .shadowHost(BY_SHADOW_HOST_1)
                                        .element(BY_NOT_EXISTING_ELEMENT)
                                        .build())
                        .addLocator(
                                ElementLocator.builder()
                                        .shadowHost(BY_SHADOW_HOST_1)
                                        .element(BY_MAIN_ELEMENT)
                                        .build(),
                                true)
                        .addLocator(
                                ElementLocator.builder()
                                        .shadowHost(BY_SHADOW_HOST_1)
                                        .element(BY_OTHER_ELEMENT)
                                        .build())
                        .addIdToBeFound(PARENT_SHADOW_HOST_1_MAIN_ELEMENT_VISIBLE)
                        .addIdToBeFound(PARENT_SHADOW_HOST_1_MAIN_ELEMENT_HIDDEN)
                        .build(),
                /*
                Iframe
                 */
                TestParams.builder("2a04597e-63e3-42d9-b460-cd87394e4885")
                        .addLocator(
                                ElementLocator.builder()
                                        .topmostIframe(BY_NOT_EXISTING_IFRAME)
                                        .element(BY_MAIN_ELEMENT)
                                        .build())
                        .addLocator(
                                ElementLocator.builder()
                                        .topmostIframe(BY_IFRAME)
                                        .element(BY_NOT_EXISTING_ELEMENT)
                                        .build())
                        .addLocator(
                                ElementLocator.builder()
                                        .topmostIframe(BY_IFRAME)
                                        .element(BY_MAIN_ELEMENT)
                                        .build(),
                                true)
                        .addLocator(
                                ElementLocator.builder()
                                        .topmostIframe(BY_IFRAME)
                                        .element(BY_OTHER_ELEMENT)
                                        .build())
                        .addIdToBeFound(IFRAME_MAIN_ELEMENT_VISIBLE)
                        .addIdToBeFound(IFRAME_MAIN_ELEMENT_HIDDEN)
                        .build(),
                /*
                Iframe + shadow host 1
                 */
                TestParams.builder("789d36f5-7bec-4ed4-9684-2a3e0084efe9")
                        .addLocator(
                                ElementLocator.builder()
                                        .topmostIframe(BY_NOT_EXISTING_IFRAME)
                                        .shadowHost(BY_SHADOW_HOST_1)
                                        .element(BY_MAIN_ELEMENT)
                                        .build())
                        .addLocator(
                                ElementLocator.builder()
                                        .topmostIframe(BY_IFRAME)
                                        .shadowHost(BY_NOT_EXISTING_SHADOW_HOST)
                                        .element(BY_MAIN_ELEMENT)
                                        .build())
                        .addLocator(
                                ElementLocator.builder()
                                        .topmostIframe(BY_IFRAME)
                                        .shadowHost(BY_SHADOW_HOST_WITHOUT_ROOT)
                                        .element(BY_MAIN_ELEMENT)
                                        .build())
                        .addLocator(
                                ElementLocator.builder()
                                        .topmostIframe(BY_IFRAME)
                                        .shadowHost(BY_NOT_EXISTING_SHADOW_HOST)
                                        .element(BY_NOT_EXISTING_ELEMENT)
                                        .build())
                        .addLocator(
                                ElementLocator.builder()
                                        .topmostIframe(BY_IFRAME)
                                        .shadowHost(BY_SHADOW_HOST_1)
                                        .element(BY_MAIN_ELEMENT)
                                        .build(),
                                true)
                        .addLocator(
                                ElementLocator.builder()
                                        .topmostIframe(BY_IFRAME)
                                        .shadowHost(BY_SHADOW_HOST_1)
                                        .element(BY_OTHER_ELEMENT)
                                        .build())
                        .addIdToBeFound(IFRAME_SHADOW_HOST_1_MAIN_ELEMENT_VISIBLE)
                        .addIdToBeFound(IFRAME_SHADOW_HOST_1_MAIN_ELEMENT_HIDDEN)
                        .build());
    }

    @Getter
    @RequiredArgsConstructor
    private static class TestParams {
        private final String testParamsId;
        private final List<ElementLocator> locators;
        private final ElementLocator expectedLocatorFound;
        private final List<String> expectedIdsForFoundElements;

        public Object[] toMethodParams() {
            return new Object[] {
                testParamsId, locators, expectedLocatorFound, expectedIdsForFoundElements
            };
        }

        static TestParamsBuilder builder(String testParamsId) {
            return new TestParamsBuilder(testParamsId);
        }

        private static class TestParamsBuilder {
            private final String testParamsId;
            private final List<ElementLocator> locators = new ArrayList<>();
            private ElementLocator expectedLocatorFound;
            private final List<String> expectedIdsForFoundElements = new ArrayList<>();

            TestParamsBuilder(String testParamsId) {
                this.testParamsId = testParamsId;
            }

            TestParamsBuilder addLocator(ElementLocator selector) {
                return addLocator(selector, false);
            }

            TestParamsBuilder addLocator(ElementLocator selector, boolean shouldBeFound) {
                locators.add(selector);
                if (shouldBeFound) {
                    expectedLocatorFound = selector;
                }
                return this;
            }

            TestParamsBuilder addIdToBeFound(String expectedElementText) {
                expectedIdsForFoundElements.add(expectedElementText);
                return this;
            }

            TestParams build() {
                return new TestParams(
                        testParamsId, locators, expectedLocatorFound, expectedIdsForFoundElements);
            }
        }
    }
}
