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

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.BankIdElementLocator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.BankIdElementsSearchQuery;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.BankIdElementsSearchResult;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.BankIdElementsSearcher;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.utils.BankIdWebDriverCommonUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.utils.Sleeper;

@RunWith(JUnitParamsRunner.class)
public class BankIdWebDriverTest {

    /*
    Mocks
     */
    private WebDriver driver;
    private WebDriver.Options driverOptions;
    private BankIdWebDriverCommonUtils driverCommonUtils;
    private BankIdElementsSearcher elementsSearcher;
    private Sleeper sleeper;

    private InOrder mocksToVerifyInOrder;

    /*
    Real
     */
    private BankIdWebDriver bankIdDriver;

    @Before
    public void setup() {
        driver = mock(WebDriver.class);
        driverOptions = mock(WebDriver.Options.class);
        when(driver.manage()).thenReturn(driverOptions);

        driverCommonUtils = mock(BankIdWebDriverCommonUtils.class);
        elementsSearcher = mock(BankIdElementsSearcher.class);

        mocksToVerifyInOrder = inOrder(driver, driverOptions, driverCommonUtils, elementsSearcher);

        bankIdDriver =
                new BankIdWebDriverImpl(driver, sleeper, driverCommonUtils, elementsSearcher);
    }

    @Test
    @Parameters(value = {"http://some.url", "https://other.url"})
    public void should_get_url(String url) {
        // when
        bankIdDriver.getUrl(url);

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
    public void should_quit_driver() {
        // when
        bankIdDriver.quitDriver();

        // then
        mocksToVerifyInOrder.verify(driver).quit();
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void should_get_cookies() {
        // given
        Cookie cookie1 = mock(Cookie.class);
        Cookie cookie2 = mock(Cookie.class);
        Set<Cookie> expectedCookies = ImmutableSet.of(cookie1, cookie2);

        when(driverOptions.getCookies()).thenReturn(expectedCookies);

        // when
        Set<Cookie> cookies = bankIdDriver.getCookies();

        // then
        assertThat(cookies).isEqualTo(expectedCookies);

        mocksToVerifyInOrder.verify(driver).manage();
        mocksToVerifyInOrder.verify(driverOptions).getCookies();
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void should_get_full_page_source_when_there_is_bank_id_iframe() {
        // given
        when(driverCommonUtils.trySwitchToIframe(any())).thenReturn(true);
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

        mocksToVerifyInOrder.verify(driverCommonUtils).switchToParentWindow();
        mocksToVerifyInOrder.verify(driver).getPageSource();
        mocksToVerifyInOrder.verify(driverCommonUtils).trySwitchToIframe(BY_IFRAME);
        mocksToVerifyInOrder.verify(driver).getPageSource();
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void should_omit_iframe_source_when_there_is_no_bank_id_iframe() {
        // given
        when(driverCommonUtils.trySwitchToIframe(any())).thenReturn(false);
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

        mocksToVerifyInOrder.verify(driverCommonUtils).switchToParentWindow();
        mocksToVerifyInOrder.verify(driver).getPageSource();
        mocksToVerifyInOrder.verify(driverCommonUtils).trySwitchToIframe(BY_IFRAME);
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void should_click_first_button_when_it_can_be_found() {
        // given
        BankIdElementLocator buttonLocator = mock(BankIdElementLocator.class);
        WebElement buttonElement1 = mock(WebElement.class);
        WebElement buttonElement2 = mock(WebElement.class);

        when(elementsSearcher.searchForFirstMatchingLocator(any()))
                .thenReturn(
                        BankIdElementsSearchResult.of(
                                buttonLocator, asList(buttonElement1, buttonElement2)));

        // when
        bankIdDriver.clickButton(buttonLocator);

        // then
        verify(buttonElement1).click();
        verifyZeroInteractions(buttonElement2);

        mocksToVerifyInOrder
                .verify(elementsSearcher)
                .searchForFirstMatchingLocator(
                        BankIdElementsSearchQuery.builder()
                                .searchFor(buttonLocator)
                                .searchForSeconds(10)
                                .build());
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void should_throw_illegal_state_exception_when_button_to_click_cannot_be_found() {
        // given
        BankIdElementLocator buttonLocator = mock(BankIdElementLocator.class);

        when(elementsSearcher.searchForFirstMatchingLocator(any()))
                .thenReturn(BankIdElementsSearchResult.empty());

        // when
        Throwable throwable = catchThrowable(() -> bankIdDriver.clickButton(buttonLocator));

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Could not find button element by " + buttonLocator);

        mocksToVerifyInOrder
                .verify(elementsSearcher)
                .searchForFirstMatchingLocator(
                        BankIdElementsSearchQuery.builder()
                                .searchFor(buttonLocator)
                                .searchForSeconds(10)
                                .build());
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    @Parameters(value = {"value1", "value2"})
    public void should_set_value_to_first_found_element(String valueToSet) {
        // given
        BankIdElementLocator elementLocator = mock(BankIdElementLocator.class);
        WebElement element1 = mock(WebElement.class);
        WebElement element2 = mock(WebElement.class);

        when(elementsSearcher.searchForFirstMatchingLocator(any()))
                .thenReturn(
                        BankIdElementsSearchResult.of(elementLocator, asList(element1, element2)));

        // when
        bankIdDriver.setValueToElement(valueToSet, elementLocator);

        // then
        verify(element1).sendKeys(valueToSet);
        verifyZeroInteractions(element2);

        mocksToVerifyInOrder
                .verify(elementsSearcher)
                .searchForFirstMatchingLocator(
                        BankIdElementsSearchQuery.builder()
                                .searchFor(elementLocator)
                                .searchForSeconds(10)
                                .build());
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void
            should_throw_illegal_state_exception_when_element_to_set_value_to_cannot_be_found() {
        // given
        BankIdElementLocator elementLocator = mock(BankIdElementLocator.class);

        when(elementsSearcher.searchForFirstMatchingLocator(any()))
                .thenReturn(BankIdElementsSearchResult.empty());

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
                        BankIdElementsSearchQuery.builder()
                                .searchFor(elementLocator)
                                .searchForSeconds(10)
                                .build());
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void should_delegate_searching_for_elements_to_elements_searcher() {
        // given
        BankIdElementsSearchResult expectedSearchResult = mock(BankIdElementsSearchResult.class);
        when(elementsSearcher.searchForFirstMatchingLocator(any()))
                .thenReturn(expectedSearchResult);

        // when
        BankIdElementsSearchQuery searchQuery = mock(BankIdElementsSearchQuery.class);
        BankIdElementsSearchResult searchResult =
                bankIdDriver.searchForFirstMatchingLocator(searchQuery);

        // then
        assertThat(searchResult).isEqualTo(expectedSearchResult);

        mocksToVerifyInOrder.verify(elementsSearcher).searchForFirstMatchingLocator(searchQuery);
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }
}
