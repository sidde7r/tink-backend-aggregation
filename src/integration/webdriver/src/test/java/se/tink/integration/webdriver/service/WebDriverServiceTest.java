package se.tink.integration.webdriver.service;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import se.tink.integration.webdriver.WebDriverWrapper;
import se.tink.integration.webdriver.service.basicutils.Sleeper;
import se.tink.integration.webdriver.service.basicutils.WebDriverBasicUtils;
import se.tink.integration.webdriver.service.proxy.ProxyManager;
import se.tink.integration.webdriver.service.searchelements.ElementLocator;
import se.tink.integration.webdriver.service.searchelements.ElementsSearchQuery;
import se.tink.integration.webdriver.service.searchelements.ElementsSearchResult;
import se.tink.integration.webdriver.service.searchelements.ElementsSearcher;

@RunWith(JUnitParamsRunner.class)
public class WebDriverServiceTest {

    private static final By EXAMPLE_BY_IFRAME = By.tagName("iframe");

    /*
    Mocks
     */
    private WebDriverWrapper driver;
    private WebDriverBasicUtils driverBasicUtils;
    private ElementsSearcher elementsSearcher;

    private InOrder mocksToVerifyInOrder;

    /*
    Real
     */
    private WebDriverService driverService;

    @Before
    public void setup() {
        driver = mock(WebDriverWrapper.class);

        driverBasicUtils = mock(WebDriverBasicUtils.class);
        elementsSearcher = mock(ElementsSearcher.class);
        Sleeper sleeper = mock(Sleeper.class);

        ProxyManager proxyManager = mock(ProxyManager.class);

        mocksToVerifyInOrder =
                inOrder(driver, driverBasicUtils, elementsSearcher, sleeper, proxyManager);

        driverService =
                new WebDriverServiceImpl(driver, driverBasicUtils, elementsSearcher, proxyManager);
    }

    @Test
    @Parameters(value = {"http://some.url", "https://other.url"})
    public void should_get_url(String url) {
        // when
        driverService.get(url);

        // then
        mocksToVerifyInOrder.verify(driver).get(url);
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    @Parameters(value = {"http://some.url", "https://other.url"})
    public void should_get_current_url(String expectedCurrentUrl) {
        // given
        when(driver.getCurrentUrl()).thenReturn(expectedCurrentUrl);

        // when
        String currentUrl = driverService.getCurrentUrl();

        // then
        assertThat(currentUrl).isEqualTo(expectedCurrentUrl);

        mocksToVerifyInOrder.verify(driver).getCurrentUrl();
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void should_get_full_page_source_when_there_is_bank_id_iframe() {
        // given
        when(driverBasicUtils.trySwitchToIframe(any())).thenReturn(true);
        when(driver.getPageSource())
                .thenReturn("parent page source")
                .thenReturn("iframe page source");

        // when
        String pageSourceLog = driverService.getFullPageSourceLog(EXAMPLE_BY_IFRAME);

        // then
        assertThat(pageSourceLog)
                .isEqualTo(
                        String.format(
                                "Main page source:%n" + "%s" + "%nIframe source:%n" + "%s",
                                "parent page source", "iframe page source"));

        mocksToVerifyInOrder.verify(driverBasicUtils).switchToParentWindow();
        mocksToVerifyInOrder.verify(driver).getPageSource();
        mocksToVerifyInOrder.verify(driverBasicUtils).trySwitchToIframe(EXAMPLE_BY_IFRAME);
        mocksToVerifyInOrder.verify(driver).getPageSource();
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void should_omit_iframe_source_when_there_is_no_bank_id_iframe() {
        // given
        when(driverBasicUtils.trySwitchToIframe(any())).thenReturn(false);
        when(driver.getPageSource())
                .thenReturn("parent page source")
                .thenReturn("iframe page source");

        // when
        String pageSourceLog = driverService.getFullPageSourceLog(EXAMPLE_BY_IFRAME);

        // then
        assertThat(pageSourceLog)
                .isEqualTo(
                        String.format(
                                "Main page source:%n" + "%s" + "%nIframe source:%n" + "%s",
                                "parent page source", null));

        mocksToVerifyInOrder.verify(driverBasicUtils).switchToParentWindow();
        mocksToVerifyInOrder.verify(driver).getPageSource();
        mocksToVerifyInOrder.verify(driverBasicUtils).trySwitchToIframe(EXAMPLE_BY_IFRAME);
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void should_click_first_button_when_it_can_be_found() {
        // given
        ElementLocator buttonLocator = mock(ElementLocator.class);
        WebElement buttonElement1 = mock(WebElement.class);
        WebElement buttonElement2 = mock(WebElement.class);

        when(elementsSearcher.searchForFirstMatchingLocator(any()))
                .thenReturn(
                        ElementsSearchResult.of(
                                buttonLocator, asList(buttonElement1, buttonElement2)));

        // when
        driverService.clickButton(buttonLocator);

        // then
        verify(buttonElement1).click();
        verifyZeroInteractions(buttonElement2);

        mocksToVerifyInOrder
                .verify(elementsSearcher)
                .searchForFirstMatchingLocator(
                        ElementsSearchQuery.builder()
                                .searchFor(buttonLocator)
                                .searchForSeconds(10)
                                .build());
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void should_throw_illegal_state_exception_when_button_to_click_cannot_be_found() {
        // given
        ElementLocator buttonLocator = mock(ElementLocator.class);

        when(elementsSearcher.searchForFirstMatchingLocator(any()))
                .thenReturn(ElementsSearchResult.empty());

        // when
        Throwable throwable = catchThrowable(() -> driverService.clickButton(buttonLocator));

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Could not find button element by " + buttonLocator);

        mocksToVerifyInOrder
                .verify(elementsSearcher)
                .searchForFirstMatchingLocator(
                        ElementsSearchQuery.builder()
                                .searchFor(buttonLocator)
                                .searchForSeconds(10)
                                .build());
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    @Parameters(value = {"value1", "value2"})
    public void should_set_value_to_first_found_element(String valueToSet) {
        // given
        ElementLocator elementLocator = mock(ElementLocator.class);
        WebElement element1 = mock(WebElement.class);
        WebElement element2 = mock(WebElement.class);

        when(elementsSearcher.searchForFirstMatchingLocator(any()))
                .thenReturn(ElementsSearchResult.of(elementLocator, asList(element1, element2)));

        // when
        driverService.setValueToElement(valueToSet, elementLocator);

        // then
        verify(element1).sendKeys(valueToSet);
        verifyZeroInteractions(element2);

        mocksToVerifyInOrder
                .verify(elementsSearcher)
                .searchForFirstMatchingLocator(
                        ElementsSearchQuery.builder()
                                .searchFor(elementLocator)
                                .searchForSeconds(10)
                                .build());
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void
            should_throw_illegal_state_exception_when_element_to_set_value_to_cannot_be_found() {
        // given
        ElementLocator elementLocator = mock(ElementLocator.class);

        when(elementsSearcher.searchForFirstMatchingLocator(any()))
                .thenReturn(ElementsSearchResult.empty());

        // when
        Throwable throwable =
                catchThrowable(() -> driverService.setValueToElement("some value", elementLocator));

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Could not find element by " + elementLocator);

        mocksToVerifyInOrder
                .verify(elementsSearcher)
                .searchForFirstMatchingLocator(
                        ElementsSearchQuery.builder()
                                .searchFor(elementLocator)
                                .searchForSeconds(10)
                                .build());
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void should_delegate_searching_for_elements_to_elements_searcher() {
        // given
        ElementsSearchResult expectedSearchResult = mock(ElementsSearchResult.class);
        when(elementsSearcher.searchForFirstMatchingLocator(any()))
                .thenReturn(expectedSearchResult);

        // when
        ElementsSearchQuery searchQuery = mock(ElementsSearchQuery.class);
        ElementsSearchResult searchResult =
                driverService.searchForFirstMatchingLocator(searchQuery);

        // then
        assertThat(searchResult).isEqualTo(expectedSearchResult);

        mocksToVerifyInOrder.verify(elementsSearcher).searchForFirstMatchingLocator(searchQuery);
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }
}
