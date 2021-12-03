package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.HtmlSelectors.BY_IFRAME;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.openqa.selenium.WebElement;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.basicutils.Sleeper;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.basicutils.WebDriverBasicUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.ElementLocator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.ElementsSearchQuery;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.ElementsSearchResult;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.ElementsSearcher;
import se.tink.integration.webdriver.WebDriverWrapper;

@RunWith(JUnitParamsRunner.class)
public class WebDriverServiceTest {

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
    private WebDriverService bankIdDriver;

    @Before
    public void setup() {
        driver = mock(WebDriverWrapper.class);

        driverBasicUtils = mock(WebDriverBasicUtils.class);
        elementsSearcher = mock(ElementsSearcher.class);
        Sleeper sleeper = mock(Sleeper.class);

        mocksToVerifyInOrder = inOrder(driver, driverBasicUtils, elementsSearcher, sleeper);

        bankIdDriver = new WebDriverServiceImpl(driver, driverBasicUtils, elementsSearcher);
    }

    @Test
    @Parameters(value = {"http://some.url", "https://other.url"})
    public void should_get_url(String url) {
        // when
        bankIdDriver.get(url);

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
        String currentUrl = bankIdDriver.getCurrentUrl();

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
        String pageSourceLog = bankIdDriver.getFullPageSourceLog();

        // then
        assertThat(pageSourceLog)
                .isEqualTo(
                        String.format(
                                "Main page source:%n" + "%s" + "%nBankID iframe source:%n" + "%s",
                                "parent page source", "iframe page source"));

        mocksToVerifyInOrder.verify(driverBasicUtils).switchToParentWindow();
        mocksToVerifyInOrder.verify(driver).getPageSource();
        mocksToVerifyInOrder.verify(driverBasicUtils).trySwitchToIframe(BY_IFRAME);
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
        String pageSourceLog = bankIdDriver.getFullPageSourceLog();

        // then
        assertThat(pageSourceLog)
                .isEqualTo(
                        String.format(
                                "Main page source:%n" + "%s" + "%nBankID iframe source:%n" + "%s",
                                "parent page source", null));

        mocksToVerifyInOrder.verify(driverBasicUtils).switchToParentWindow();
        mocksToVerifyInOrder.verify(driver).getPageSource();
        mocksToVerifyInOrder.verify(driverBasicUtils).trySwitchToIframe(BY_IFRAME);
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
        bankIdDriver.clickButton(buttonLocator);

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
        Throwable throwable = catchThrowable(() -> bankIdDriver.clickButton(buttonLocator));

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
        bankIdDriver.setValueToElement(valueToSet, elementLocator);

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
                catchThrowable(() -> bankIdDriver.setValueToElement("some value", elementLocator));

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
        ElementsSearchResult searchResult = bankIdDriver.searchForFirstMatchingLocator(searchQuery);

        // then
        assertThat(searchResult).isEqualTo(expectedSearchResult);

        mocksToVerifyInOrder.verify(elementsSearcher).searchForFirstMatchingLocator(searchQuery);
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }
}
