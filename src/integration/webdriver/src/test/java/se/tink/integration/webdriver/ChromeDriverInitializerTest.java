package se.tink.integration.webdriver;

import static org.assertj.core.api.Assertions.assertThat;

import com.browserup.bup.BrowserUpProxy;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;

public class ChromeDriverInitializerTest {

    @Test
    public void shouldInitializeWebDriver() {
        // given
        WebDriver webDriver = ChromeDriverInitializer.constructChromeDriver();

        // when
        webDriver.get("https://example.com/");

        // then
        assertThat(webDriver.getCurrentUrl()).isEqualTo("https://example.com/");

        ChromeDriverInitializer.quitChromeDriver(webDriver);
    }

    @Test
    @SuppressWarnings("all")
    public void shouldInitializeWebDriverWithProxy() {
        // given
        BrowserUpProxy proxy = ProxyInitializer.startProxyServer();
        Proxy seleniumProxy = ProxyInitializer.toSeleniumProxy(proxy);
        WebDriver webDriver = ChromeDriverInitializer.constructChromeDriver(seleniumProxy);

        AtomicBoolean responseReceived = new AtomicBoolean(false);
        proxy.addResponseFilter(
                (response, contents, messageInfo) -> {
                    responseReceived.set(true);
                });

        // when
        webDriver.get("https://example.com/");

        // then
        assertThat(webDriver.getCurrentUrl()).isEqualTo("https://example.com/");
        assertThat(responseReceived).isTrue();

        ProxyInitializer.shutDownProxy(proxy);
        ChromeDriverInitializer.quitChromeDriver(webDriver);
    }

    @Test
    public void getListArgumentsShouldReturnArgumentsIncludingHeadlessMode() {
        // given
        // when
        List<String> arguments =
                ChromeDriverInitializer.getListArguments("dummyAgent", "dummyLanguage");

        // then
        assertThat(arguments).hasSize(13);
        assertThat(arguments).contains("--headless");
        assertThat(arguments).contains("--blink-settings=imagesEnabled=false");
    }
}
