package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.junit.Ignore;
import org.mockito.exceptions.base.MockitoException;
import org.openqa.selenium.WebElement;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.BankIdWebDriver;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.proxy.ResponseFromProxy;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.BankIdElementLocator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.BankIdElementsSearchResult;

@Ignore
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BankIdTestUtils {

    public static void verifyThatFromUsersPerspectiveThrowableIsTheSameAsGivenAgentException(
            Throwable t, AgentException agentException) {

        assertThat(t).isInstanceOf(AgentException.class);

        AgentException e = (AgentException) t;
        assertThat(e.getError()).isEqualTo(agentException.getError());
        assertThat(e.getUserMessage().get()).isEqualTo(agentException.getUserMessage().get());
    }

    public static WebElement mockWebElement() {
        return mock(WebElement.class);
    }

    public static WebElement mockWebElementWithText(String text) {
        WebElement element = mock(WebElement.class);
        when(element.getText()).thenReturn(text);
        return element;
    }

    public static void verifyNTimes(Runnable runnable, int times) {
        for (int i = 0; i < times; i++) {
            try {
                runnable.run();
            } catch (Throwable e) {
                String message =
                        String.format(
                                "Verification failed. Verification number (staring from 1): %d / %d",
                                i + 1, times);
                throw new MockitoException(message, e);
            }
        }
    }

    public static void mockLocatorExists(BankIdElementLocator locator, BankIdWebDriver driver) {
        WebElement element = mockWebElement();
        mockLocatorExists(locator, element, driver);
    }

    public static void mockLocatorExists(
            BankIdElementLocator locator, WebElement elementForLocator, BankIdWebDriver driver) {
        mockLocatorExists(locator, singletonList(elementForLocator), driver);
    }

    public static void mockLocatorExists(
            BankIdElementLocator locator,
            List<WebElement> elementsForLocator,
            BankIdWebDriver driver) {
        doReturn(BankIdElementsSearchResult.of(locator, elementsForLocator))
                .when(driver)
                .searchForFirstMatchingLocator(
                        argThat(
                                argument -> {
                                    List<BankIdElementLocator> locatorsInQuery =
                                            argument.getLocators();
                                    return locatorsInQuery.contains(locator);
                                }));
    }

    public static void mockLocatorDoesNotExists(
            BankIdElementLocator locator, BankIdWebDriver driver) {
        doReturn(BankIdElementsSearchResult.empty())
                .when(driver)
                .searchForFirstMatchingLocator(
                        argThat(
                                argument -> {
                                    List<BankIdElementLocator> locatorsInQuery =
                                            argument.getLocators();
                                    return locatorsInQuery.contains(locator);
                                }));
    }

    public static ResponseFromProxy mockProxyResponseWithHeaders(Map<String, String> headers) {
        HttpHeaders httpHeaders = new DefaultHttpHeaders();
        headers.forEach(httpHeaders::add);

        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponse.headers()).thenReturn(httpHeaders);

        ResponseFromProxy proxyResponse = mock(ResponseFromProxy.class);
        when(proxyResponse.getResponse()).thenReturn(httpResponse);
        return proxyResponse;
    }
}
