package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collections;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.WebElement;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.authenticator.bankmappers.AuthenticationType;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.config.SdcNoConfiguration;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.libraries.selenium.WebDriverHelper;
import se.tink.libraries.selenium.exceptions.HtmlElementNotFoundException;
import se.tink.libraries.selenium.exceptions.ScreenScrapingException;

public class PostAuthDriverProcessorTest {
    private static final By TARGET_ELEMENT_XPATH = By.xpath("//input[@value='Logg ut']");

    private PostAuthDriverProcessor objUnderTest;
    private WebDriver driverMock;
    private WebDriverHelper webDriverHelperMock;
    private SdcNoConfiguration configMock;

    @Before
    public void initSetup() {
        driverMock = Mockito.mock(WebDriver.class);
        webDriverHelperMock = Mockito.mock(WebDriverHelper.class);
        configMock = Mockito.mock(SdcNoConfiguration.class);
        TinkHttpClient clientMock = Mockito.mock(TinkHttpClient.class);
        objUnderTest =
                new PostAuthDriverProcessor(
                        driverMock, webDriverHelperMock, clientMock, configMock);

        Options options = Mockito.mock(Options.class);
        given(options.getCookies()).willReturn(Collections.emptySet());
        given(driverMock.manage()).willReturn(options);
    }

    @Test
    public void processWebDriverShouldThrowScreenScrapingExWhenTargetElementNotFound() {
        // given
        given(webDriverHelperMock.getElement(driverMock, TARGET_ELEMENT_XPATH))
                .willThrow(new HtmlElementNotFoundException(""));
        given(driverMock.findElements(TARGET_ELEMENT_XPATH)).willReturn(Collections.emptyList());
        given(configMock.getAuthenticationType()).willReturn(AuthenticationType.PORTAL);

        // when
        Throwable throwable = Assertions.catchThrowable(() -> objUnderTest.processWebDriver());
        // then
        assertThat(throwable)
                .isInstanceOf(ScreenScrapingException.class)
                .hasMessage("Website for cookies for credit card fetching not reached");
    }

    @Test
    public void setCookiesToClientShouldBeCalledTwiceWhenAuthenticationTypePortal() {
        // given
        WebElement element = Mockito.mock(WebElement.class);
        given(element.isDisplayed()).willReturn(true);
        given(driverMock.findElements(any())).willReturn(Arrays.asList(element));
        given(configMock.getAuthenticationType()).willReturn(AuthenticationType.PORTAL);

        // when
        objUnderTest.processWebDriver();

        // then
        verify(driverMock, times(2)).manage();
    }

    @Test
    public void setCookiesToClientShouldBeCalledOnceWhenAuthenticationTypeNotPortal() {
        // given
        WebElement element = Mockito.mock(WebElement.class);
        given(element.isDisplayed()).willReturn(true);
        given(driverMock.findElements(any())).willReturn(Arrays.asList(element));
        given(configMock.getAuthenticationType()).willReturn(AuthenticationType.NETTBANK);

        // when
        objUnderTest.processWebDriver();

        // then
        verify(driverMock).manage();
    }
}
