package se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.function.Consumer;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.authenticator.exception.GlobalPositionNotFoundException;
import se.tink.backend.aggregation.nxgen.http.log.executor.raw.RawHttpTrafficLogger;
import se.tink.backend.aggregation.nxgen.storage.AgentTemporaryStorage;
import se.tink.integration.webdriver.WebDriverWrapper;
import se.tink.libraries.har_logger.src.model.HarEntry;

public class RuralviaAuthenticatorTest {
    private RuralviaAuthenticator ruralviaAuthenticator;
    private WebDriverWrapper driver;
    private Credentials credentials;

    @Before
    public void init() {
        credentials = mock(Credentials.class);
        when(credentials.getField(any(Field.Key.class))).thenReturn("dummyKey");
        RuralviaApiClient apiClient = mock(RuralviaApiClient.class);
        driver = createMockedWebDriver();
        AgentTemporaryStorage storage = mock(AgentTemporaryStorage.class);
        RawHttpTrafficLogger rawHttpTrafficLogger = mock(RawHttpTrafficLogger.class);
        Consumer<HarEntry> harEntryConsumer = mock(Consumer.class);
        ruralviaAuthenticator =
                new RuralviaAuthenticator(
                        apiClient, storage, rawHttpTrafficLogger, harEntryConsumer, driver);
    }

    private WebDriverWrapper createMockedWebDriver() {
        WebDriverWrapper webDriverWrapper = mock(WebDriverWrapper.class);
        return webDriverWrapper;
    }

    @Test
    public void authenticate_should_retry_login_3_times_when_HtmlElementFoundExcepion_thrown() {
        // given
        WebElement element = mock(WebElement.class);
        when(element.isDisplayed()).thenThrow(NoSuchElementException.class);

        when(driver.findElement(any())).thenReturn(element);
        when(element.findElement(any())).thenReturn(element);
        when(driver.getCurrentUrl()).thenReturn("");
        when(driver.getPageSource()).thenReturn("");
        when(driver.executeScript(any())).thenReturn("complete");

        // when
        Throwable throwable = catchThrowable(() -> ruralviaAuthenticator.authenticate(credentials));

        // then
        assertThat(throwable).isInstanceOf(GlobalPositionNotFoundException.class);
        verify(driver, times(3)).get(any());
    }
}
