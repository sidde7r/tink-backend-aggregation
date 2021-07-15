package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.authenticator;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.authenticator.PostAuthDriverProcessor.AGREEMENT_LIST;
import static se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.authenticator.PostAuthDriverProcessor.AGREEMENT_LIST_FIRST_OPTION;
import static se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.authenticator.PostAuthDriverProcessor.ERROR_MESSAGE_CONTENT;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.WebElement;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.config.AuthenticationType;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.config.SdcNoConfiguration;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.config.SdcNoConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.screenscraping.WebScrapingConstants;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.integration.webdriver.ChromeDriverInitializer;
import se.tink.integration.webdriver.WebDriverHelper;
import se.tink.integration.webdriver.exceptions.HtmlElementNotFoundException;

@RunWith(JUnitParamsRunner.class)
public class PostAuthDriverProcessorTest {

    private static final String BASE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/no/banks/sdcno/resources";
    private static final String SURVEY_AND_ACCEPT_COOKIES =
            Paths.get(BASE_PATH, "antiMoneyLaunderingSurveyAndCookies.html").toUri().toString();
    private static final String LOGGED_IN =
            Paths.get(BASE_PATH, "loggedIn.html").toUri().toString();
    private static final String LOG_IN_WITH_BANK_ID =
            Paths.get(BASE_PATH, "logInWithBankID.html").toUri().toString();

    private static final By TARGET_ELEMENT_XPATH = By.xpath("//input[@value='Logg ut']");

    private static WebDriver testDriver;

    private PostAuthDriverProcessor objUnderTest;
    private WebDriver driverMock;
    private WebDriverHelper webDriverHelperMock;
    private SdcNoConfiguration configMock;

    @BeforeClass
    public static void setupDriver() {
        testDriver = ChromeDriverInitializer.constructChromeDriver(WebScrapingConstants.USER_AGENT);
    }

    @AfterClass
    public static void quitDriver() {
        ChromeDriverInitializer.quitChromeDriver(testDriver);
    }

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
    public void processWebDriverWhenTargetElementNotFound() {
        // given
        given(webDriverHelperMock.waitForElement(driverMock, TARGET_ELEMENT_XPATH))
                .willThrow(new HtmlElementNotFoundException(""));
        given(driverMock.findElements(TARGET_ELEMENT_XPATH)).willReturn(Collections.emptyList());
        given(configMock.getAuthenticationType()).willReturn(AuthenticationType.PORTAL);

        // when
        objUnderTest.processWebDriver();
        // then
        verify(driverMock).manage();
    }

    @Test
    public void setCookiesToClientShouldBeCalledTwiceWhenAuthenticationTypePortal() {
        // given
        WebElement element = Mockito.mock(WebElement.class);
        given(element.isDisplayed()).willReturn(true);
        given(driverMock.findElements(any())).willReturn(singletonList(element));
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
        given(driverMock.findElements(any())).willReturn(singletonList(element));
        given(configMock.getAuthenticationType()).willReturn(AuthenticationType.NETTBANK);

        // when
        objUnderTest.processWebDriver();

        // then
        verify(driverMock).manage();
    }

    @Test
    public void afterAuthenticationShouldCheckForErrorMessageAndMultipleAgreements() {
        // given
        mockElementDoesntExist(ERROR_MESSAGE_CONTENT);
        mockElementDoesntExist(AGREEMENT_LIST);

        // when
        objUnderTest.processLogonCasesAfterSuccessfulBankIdAuthentication();

        // then
        verify(driverMock).findElements(ERROR_MESSAGE_CONTENT);
        verify(driverMock).findElements(AGREEMENT_LIST);
    }

    @Test
    @Parameters(method = "shouldThrowCorrectExceptionForErrorMessageParams")
    public void afterAuthenticationShouldFindErrorMessageAndThrowCorrectException(
            String errorMessage, AgentException agentException) {
        // given
        mockElementExists(ERROR_MESSAGE_CONTENT, errorMessage);

        // when
        Throwable throwable =
                catchThrowable(
                        () -> objUnderTest.processLogonCasesAfterSuccessfulBankIdAuthentication());

        // then
        assertThat(throwable).isEqualToComparingFieldByFieldRecursively(agentException);

        verify(driverMock).findElements(ERROR_MESSAGE_CONTENT);
        verify(driverMock, times(0)).findElements(AGREEMENT_LIST);
    }

    @SuppressWarnings("unused")
    private static Object[] shouldThrowCorrectExceptionForErrorMessageParams() {
        Stream<ErrorMessageTestParams> knownExceptions =
                Stream.of(
                                ErrorMessageTestParams.builder()
                                        .errorMessage(
                                                SdcNoConstants.ErrorMessages.NO_ACCOUNT_FOR_BANK_ID)
                                        .expectedException(LoginError.NOT_CUSTOMER.exception())
                                        .build(),
                                ErrorMessageTestParams.builder()
                                        .errorMessage(
                                                SdcNoConstants.ErrorMessages.BANK_TEMPORARY_ERROR)
                                        .expectedException(
                                                BankServiceError.BANK_SIDE_FAILURE.exception())
                                        .build())
                        .map(
                                params ->
                                        asList(
                                                params.modifyErrorMessage(String::toLowerCase),
                                                params.modifyErrorMessage(String::toUpperCase),
                                                params.modifyErrorMessage(
                                                        message ->
                                                                "some prefix !@#"
                                                                        + message
                                                                        + "some suffix 123")))
                        .flatMap(List::stream);

        Stream<ErrorMessageTestParams> otherExceptions =
                Stream.of(
                        ErrorMessageTestParams.builder()
                                .errorMessage("!@$%#^$&")
                                .expectedException(LoginError.DEFAULT_MESSAGE.exception())
                                .build(),
                        ErrorMessageTestParams.builder()
                                .errorMessage("")
                                .expectedException(LoginError.DEFAULT_MESSAGE.exception())
                                .build());

        return Stream.concat(knownExceptions, otherExceptions)
                .map(ErrorMessageTestParams::toMethodParams)
                .toArray();
    }

    @Builder
    @RequiredArgsConstructor
    private static class ErrorMessageTestParams {

        private final String errorMessage;
        private final AgentException expectedException;

        public ErrorMessageTestParams modifyErrorMessage(Function<String, String> messageModifier) {
            String newMessage = messageModifier.apply(errorMessage);
            return new ErrorMessageTestParams(newMessage, expectedException);
        }

        public Object[] toMethodParams() {
            return new Object[] {errorMessage, expectedException};
        }
    }

    @Test
    public void afterAuthenticationShouldFindAndClickFirstAgreement() {
        // given
        mockElementDoesntExist(ERROR_MESSAGE_CONTENT);

        WebElement agreementsListElement = mockElementExists(AGREEMENT_LIST);
        WebElement agreementsListFirstOption =
                mockElementExistsInElement(AGREEMENT_LIST_FIRST_OPTION, agreementsListElement);

        // when
        objUnderTest.processLogonCasesAfterSuccessfulBankIdAuthentication();

        // then
        verify(driverMock).findElements(ERROR_MESSAGE_CONTENT);
        verify(driverMock).findElements(AGREEMENT_LIST);
        verify(agreementsListElement).findElements(AGREEMENT_LIST_FIRST_OPTION);
        verify(agreementsListFirstOption).click();
    }

    @Test
    public void afterAuthenticationShouldThrowExceptionWhenCannotFindFirstAgreement() {
        // given
        mockElementDoesntExist(ERROR_MESSAGE_CONTENT);

        WebElement agreementsListElement = mockElementExists(AGREEMENT_LIST);
        when(agreementsListElement.findElements(AGREEMENT_LIST_FIRST_OPTION))
                .thenReturn(emptyList());

        // when
        Throwable throwable =
                catchThrowable(
                        () -> objUnderTest.processLogonCasesAfterSuccessfulBankIdAuthentication());

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageStartingWith(
                        "Could not find first agreements option, verify page source:");

        verify(driverMock).findElements(ERROR_MESSAGE_CONTENT);
        verify(driverMock).findElements(AGREEMENT_LIST);
        verify(agreementsListElement).findElements(AGREEMENT_LIST_FIRST_OPTION);
    }

    @Test
    public void shouldPostponeAntiMoneyLaunderingSurveyAndAcceptCookies() {
        // given
        WebElement postponeButton = mock(WebElement.class);
        List<WebElement> postponeButtons = Collections.singletonList(postponeButton);
        WebElement acceptCookiesButton = mock(WebElement.class);
        List<WebElement> cookiesButtons = Collections.singletonList(acceptCookiesButton);

        // and
        given(driverMock.findElements(PostAuthDriverProcessor.ACCEPT_COOKIES_BUTTON))
                .willReturn(cookiesButtons);
        given(driverMock.findElements(PostAuthDriverProcessor.POSTPONE_SURVEY_BUTTON))
                .willReturn(postponeButtons);

        // when
        objUnderTest.processLogonCasesAfterSuccessfulBankIdAuthentication();

        // then
        verify(driverMock).findElements(PostAuthDriverProcessor.ACCEPT_COOKIES_BUTTON);
        verify(cookiesButtons.get(0)).click();
        verify(driverMock).findElements(PostAuthDriverProcessor.POSTPONE_SURVEY_BUTTON);
        verify(postponeButtons.get(0)).click();
        verify(webDriverHelperMock, times(2)).sleep(2000);
    }

    @Test
    public void shouldFindPostponeSurveyAndCookiesButton() {
        // given
        testDriver.get(SURVEY_AND_ACCEPT_COOKIES);

        // when & then
        assertThat(testDriver.findElements(PostAuthDriverProcessor.POSTPONE_SURVEY_BUTTON))
                .hasSize(1);
        assertThat(testDriver.findElements(PostAuthDriverProcessor.ACCEPT_COOKIES_BUTTON))
                .hasSize(1);
    }

    @Test
    @Parameters(method = "viewsWithoutSurveyAndCookiesButton")
    public void shouldNotFindSurveyOrCookiesButton(String url) {
        // given
        testDriver.get(url);

        // when & then
        assertThat(testDriver.findElements(PostAuthDriverProcessor.POSTPONE_SURVEY_BUTTON))
                .isEmpty();
        assertThat(testDriver.findElements(PostAuthDriverProcessor.ACCEPT_COOKIES_BUTTON))
                .isEmpty();
    }

    private Object[] viewsWithoutSurveyAndCookiesButton() {
        return new Object[] {
            new Object[] {
                LOGGED_IN,
            },
            new Object[] {
                LOG_IN_WITH_BANK_ID,
            },
        };
    }

    private void mockElementDoesntExist(By selector) {
        when(driverMock.findElements(eq(selector))).thenReturn(emptyList());
    }

    @SuppressWarnings("SameParameterValue")
    private WebElement mockElementExists(By selector) {
        return mockElementExists(selector, null);
    }

    @SuppressWarnings("SameParameterValue")
    private WebElement mockElementExistsInElement(By selector, WebElement containingElement) {
        return mockElementExists(selector, containingElement, null);
    }

    private WebElement mockElementExists(By selector, String elementText) {
        return mockElementExists(selector, driverMock, elementText);
    }

    private WebElement mockElementExists(
            By selector, SearchContext searchContext, String elementText) {
        WebElement element = mock(WebElement.class);
        when(element.getAttribute(eq("innerText"))).thenReturn(elementText);

        when(searchContext.findElements(eq(selector))).thenReturn(singletonList(element));

        return element;
    }
}
