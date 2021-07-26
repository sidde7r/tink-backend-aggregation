package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.authenticator;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver.Navigation;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.WebElement;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.LoginForm;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.Paths;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.ScaForm;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.authenticator.page.AttemptsLimitExceededException;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.storage.AgentTemporaryStorage;
import se.tink.integration.webdriver.WebDriverWrapper;

@RunWith(MockitoJUnitRunner.class)
public class BankinterAuthenticationClientTest {
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    @Mock private WebDriverWrapper driver;
    @Mock private HtmlLogger htmlLogger;
    @Mock private BankinterApiClient apiClient;
    @Mock private SupplementalInformationHelper supplementalInformationHelper;
    @Mock private AgentTemporaryStorage agentTemporaryStorage;
    private BankinterAuthenticationClient authenticationClient;

    @Before
    public void setUp() throws Exception {
        this.authenticationClient =
                new BankinterAuthenticationClient(
                        driver, agentTemporaryStorage, htmlLogger, apiClient);
    }

    @Test
    public void shouldLoginUserProperlyWithoutErrors() throws AttemptsLimitExceededException {
        // given
        String loginPageUrl = "loginPageUrl";
        Navigation navigation = mock(Navigation.class);
        when(driver.navigate()).thenReturn(navigation);
        when(driver.findElement(By.id(LoginForm.FORM_ID))).thenReturn(mock(WebElement.class));
        when(driver.findElement(By.id(LoginForm.USERNAME_FIELD)))
                .thenReturn(mock(WebElement.class));
        when(driver.findElement(By.id(LoginForm.PASSWORD_FIELD)))
                .thenReturn(mock(WebElement.class));
        WebElement errorPanel = mock(WebElement.class);
        when(driver.findElement(By.id(LoginForm.ERROR_PANEL))).thenReturn(errorPanel);
        when(errorPanel.isDisplayed()).thenReturn(true);
        when(driver.getCurrentUrl()).thenReturn(loginPageUrl);

        // when
        authenticationClient.login(USERNAME, PASSWORD);

        // then
        verify(htmlLogger, times(0)).error(any());
    }

    @Test
    public void shouldThrowAttemptsLimitExceededExceptionWhenElementDoesNotExistsOnLoginPage() {
        // given
        Navigation navigation = mock(Navigation.class);
        when(driver.navigate()).thenReturn(navigation);
        when(driver.findElement(By.id(LoginForm.FORM_ID))).thenThrow(NoSuchElementException.class);

        // when
        ThrowingCallable result = () -> authenticationClient.login(USERNAME, PASSWORD);

        // then
        assertThatThrownBy(result).isInstanceOf(AttemptsLimitExceededException.class);
        verify(htmlLogger, times(3)).error("Could not find an element `By.id: loginForm`");
    }

    @Test
    public void shouldSubmitScaFormProperly() throws AttemptsLimitExceededException {
        // given
        String scaPageUrl = "scaPageUrl";
        when(supplementalInformationHelper.waitForOtpInput()).thenReturn("code");
        when(driver.findElement(By.cssSelector(ScaForm.CODE_FIELD_SELECTOR)))
                .thenReturn(mock(WebElement.class));
        when(driver.findElement(By.cssSelector(ScaForm.SUBMIT_BUTTON_SELECTOR)))
                .thenReturn(mock(WebElement.class));
        when(driver.getCurrentUrl()).thenReturn(scaPageUrl);
        WebElement errorPanel = mock(WebElement.class);
        when(driver.findElement(By.id(LoginForm.ERROR_PANEL))).thenReturn(errorPanel);
        when(errorPanel.isDisplayed()).thenReturn(true);

        // when
        authenticationClient.submitSca(supplementalInformationHelper);

        // then
        verify(htmlLogger, times(0)).error(any());
    }

    @Test
    public void shouldThrowLoginExceptionWhenVerificationCodeDoNotProvide() {
        // given
        when(driver.findElement(By.cssSelector(ScaForm.CODE_FIELD_SELECTOR)))
                .thenReturn(mock(WebElement.class));
        when(driver.findElement(By.cssSelector(ScaForm.SUBMIT_BUTTON_SELECTOR)))
                .thenReturn(mock(WebElement.class));
        when(supplementalInformationHelper.waitForOtpInput())
                .thenThrow(SupplementalInfoException.class);

        // when
        ThrowingCallable result =
                () -> authenticationClient.submitSca(supplementalInformationHelper);

        // then
        assertThatThrownBy(result).isInstanceOf(LoginException.class);
    }

    @Test
    public void shouldFinishAuthenticationProcess() {
        // given
        WebElement webElement = mock(WebElement.class);
        when(driver.findElement(By.id(LoginForm.ERROR_PANEL))).thenReturn(webElement);
        when(webElement.isDisplayed()).thenReturn(false);
        when(driver.getCurrentUrl()).thenReturn(Paths.GLOBAL_POSITION);
        Options options = mock(Options.class);
        when(driver.manage()).thenReturn(options);
        when(options.getCookies()).thenReturn(Collections.emptySet());

        // when
        authenticationClient.finishProcess();

        // then
        verify(apiClient, times(1)).storeLoginCookies(Collections.emptySet());
        verify(driver, times(1)).quit();
    }

    @Test
    public void shouldThrowIncorrectCredentialsExceptionWhenPageIsShowingError() {
        // given
        WebElement webElement = mock(WebElement.class);
        when(driver.findElement(By.id(LoginForm.ERROR_PANEL))).thenReturn(webElement);
        when(webElement.isDisplayed()).thenReturn(true);

        // when
        ThrowingCallable result = () -> authenticationClient.finishProcess();

        // then
        assertThatThrownBy(result).isInstanceOf(LoginException.class);
    }

    @Test
    public void shouldThrowNotSupportedExceptionWhenPageIsShowingUnknownError() {
        // given
        WebElement webElement = mock(WebElement.class);
        when(driver.findElement(By.id(LoginForm.ERROR_PANEL))).thenReturn(webElement);
        when(webElement.isDisplayed()).thenReturn(false);
        when(driver.getCurrentUrl()).thenReturn("differentPage");

        // when
        ThrowingCallable result = () -> authenticationClient.finishProcess();

        // then
        assertThatThrownBy(result).isInstanceOf(LoginException.class);
    }
}
