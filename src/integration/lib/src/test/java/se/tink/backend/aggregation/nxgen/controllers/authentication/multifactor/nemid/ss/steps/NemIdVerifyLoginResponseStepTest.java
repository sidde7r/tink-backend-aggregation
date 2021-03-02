package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.NEMID_CODE_APP_SCREEN;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.NEMID_CODE_CARD_SCREEN;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.NEMID_CODE_TOKEN_SCREEN;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.NEMID_WIDE_INFO_HEADING;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.NOT_EMPTY_ERROR_MESSAGE;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.NOT_EMPTY_NEMID_TOKEN;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.NemIdVerifyLoginResponseStep.ELEMENTS_TO_SEARCH_FOR_IN_IFRAME;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.util.NemIdTestHelper.asArray;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.util.NemIdTestHelper.asList;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.util.NemIdTestHelper.joinLists;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.util.NemIdTestHelper.nemIdMetricsMock;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.util.NemIdTestHelper.verifyThatFromUsersPerspectiveThrowableIsTheSameAsGivenAgentException;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.util.NemIdTestHelper.webElementMock;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.util.NemIdTestHelper.webElementMockWithText;

import com.google.common.base.CaseFormat;
import java.util.List;
import java.util.stream.Collectors;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.nemid.NemIdError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemId2FAMethodScreen;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdCredentialsStatusUpdater;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdTokenValidator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.utils.ElementsSearchQuery;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.utils.ElementsSearchResult;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.utils.NemIdWebDriverWrapper;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.util.ErrorTextTestParams;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.NemIdCodeAppConstants.UserMessage;

@RunWith(JUnitParamsRunner.class)
public class NemIdVerifyLoginResponseStepTest {

    private NemIdWebDriverWrapper driverWrapper;
    private NemIdCredentialsStatusUpdater statusUpdater;
    private NemIdTokenValidator tokenValidator;

    private NemIdVerifyLoginResponseStep verifyLoginResponseStep;

    private Credentials credentials;
    private InOrder mocksToVerifyInOrder;

    @Before
    public void setup() {
        driverWrapper = mock(NemIdWebDriverWrapper.class);
        statusUpdater = mock(NemIdCredentialsStatusUpdater.class);
        tokenValidator = mock(NemIdTokenValidator.class);

        verifyLoginResponseStep =
                new NemIdVerifyLoginResponseStep(
                        driverWrapper, nemIdMetricsMock(), statusUpdater, tokenValidator);

        credentials = mock(Credentials.class);
        mocksToVerifyInOrder = Mockito.inOrder(driverWrapper, tokenValidator, statusUpdater);
    }

    @Test
    @Parameters(method = "all2FAWebElementsWithExpected2FAScreen")
    public void should_return_correct_2FA_screen(
            By elementThatWillBeFound, NemId2FAMethodScreen expectedScreen) {
        // given
        when(driverWrapper.searchForFirstElement(any()))
                .thenReturn(ElementsSearchResult.of(elementThatWillBeFound, webElementMock()));

        // when
        NemId2FAMethodScreen nemId2FAMethodScreen =
                verifyLoginResponseStep.checkLoginResultAndGetDefault2FAScreen(credentials);

        // then
        assertThat(nemId2FAMethodScreen).isEqualTo(expectedScreen);

        mocksToVerifyInOrder
                .verify(driverWrapper)
                .searchForFirstElement(
                        ElementsSearchQuery.builder()
                                .searchInParentWindow(NOT_EMPTY_NEMID_TOKEN)
                                .searchInAnIframe(ELEMENTS_TO_SEARCH_FOR_IN_IFRAME)
                                .searchForSeconds(30)
                                .build());
        mocksToVerifyInOrder
                .verify(statusUpdater)
                .updateStatusPayload(credentials, UserMessage.VALID_CREDS);
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @SuppressWarnings("unused")
    private Object[] all2FAWebElementsWithExpected2FAScreen() {
        return new Object[] {
            asArray(NEMID_CODE_APP_SCREEN, NemId2FAMethodScreen.CODE_APP_SCREEN),
            asArray(NEMID_CODE_CARD_SCREEN, NemId2FAMethodScreen.CODE_CARD_SCREEN),
            asArray(NEMID_CODE_TOKEN_SCREEN, NemId2FAMethodScreen.CODE_TOKEN_SCREEN)
        };
    }

    @Test
    @Parameters(method = "errorMessageTextsWithExpectedException")
    public void should_fail_when_there_is_a_not_empty_error_message(
            String errorText, AgentException expectedException) {
        // given
        WebElement errorElement = webElementMockWithText(errorText);
        when(driverWrapper.searchForFirstElement(any()))
                .thenReturn(ElementsSearchResult.of(NOT_EMPTY_ERROR_MESSAGE, errorElement));

        // when
        Throwable throwable =
                catchThrowable(
                        () ->
                                verifyLoginResponseStep.checkLoginResultAndGetDefault2FAScreen(
                                        credentials));

        // then
        verifyThatFromUsersPerspectiveThrowableIsTheSameAsGivenAgentException(
                throwable, expectedException);
        mocksToVerifyInOrder
                .verify(driverWrapper)
                .searchForFirstElement(
                        ElementsSearchQuery.builder()
                                .searchInParentWindow(NOT_EMPTY_NEMID_TOKEN)
                                .searchInAnIframe(ELEMENTS_TO_SEARCH_FOR_IN_IFRAME)
                                .searchForSeconds(30)
                                .build());
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @SuppressWarnings({"unused", "unchecked"})
    private static Object[] errorMessageTextsWithExpectedException() {
        List<ErrorTextTestParams> knownErrorCases =
                asList(
                                ErrorTextTestParams.of(
                                        "incorrect user",
                                        LoginError.INCORRECT_CREDENTIALS.exception()),
                                ErrorTextTestParams.of(
                                        "incorrect password",
                                        LoginError.INCORRECT_CREDENTIALS.exception()),
                                ErrorTextTestParams.of(
                                        "fejl i bruger",
                                        LoginError.INCORRECT_CREDENTIALS.exception()),
                                ErrorTextTestParams.of(
                                        "fejl i adgangskode",
                                        LoginError.INCORRECT_CREDENTIALS.exception()),
                                ErrorTextTestParams.of(
                                        "indtast bruger",
                                        LoginError.INCORRECT_CREDENTIALS.exception()),
                                ErrorTextTestParams.of(
                                        "indtast adgangskode",
                                        LoginError.INCORRECT_CREDENTIALS.exception()),
                                ErrorTextTestParams.of(
                                        "enter activation password",
                                        LoginError.INCORRECT_CREDENTIALS.exception(
                                                UserMessage.ENTER_ACTIVATION_PASSWORD.getKey())))
                        .stream()
                        .map(
                                errorTextTestParams ->
                                        asList(
                                                errorTextTestParams,
                                                errorTextTestParams.modifyErrorText(
                                                        String::toUpperCase),
                                                errorTextTestParams.modifyErrorText(
                                                        StringUtils::capitalize),
                                                errorTextTestParams.changeErrorTextCase(
                                                        CaseFormat.LOWER_CAMEL),
                                                errorTextTestParams.changeErrorTextCase(
                                                        CaseFormat.UPPER_CAMEL),
                                                errorTextTestParams.addErrorTextSuffix(
                                                        "!@$#%$^%&^%*")))
                        .flatMap(List::stream)
                        .collect(Collectors.toList());

        List<ErrorTextTestParams> unknownErrorCases =
                knownErrorCases.stream()
                        .map(
                                errorTextTestParams ->
                                        asList(
                                                errorTextTestParams.addErrorTextPrefix("@#%"),
                                                errorTextTestParams.changeErrorTextCompletely(""),
                                                errorTextTestParams.changeErrorTextCompletely(" "),
                                                errorTextTestParams.changeErrorTextCompletely(
                                                        "some unknown text")))
                        .flatMap(List::stream)
                        .map(
                                errorTextTestParams ->
                                        errorTextTestParams.changeExpectedError(
                                                LoginError.CREDENTIALS_VERIFICATION_ERROR
                                                        .exception()))
                        .collect(Collectors.toList());

        return joinLists(knownErrorCases, unknownErrorCases).stream()
                .map(ErrorTextTestParams::toTestParameters)
                .toArray();
    }

    @Test
    @Parameters(method = "errorHeadingTextsWithExpectedException")
    public void should_fail_when_there_is_a_nem_id_info_heading(
            String errorText, AgentException expectedException) {
        // given
        WebElement headingElement = webElementMockWithText(errorText);
        when(driverWrapper.searchForFirstElement(any()))
                .thenReturn(ElementsSearchResult.of(NEMID_WIDE_INFO_HEADING, headingElement));

        // when
        Throwable throwable =
                catchThrowable(
                        () ->
                                verifyLoginResponseStep.checkLoginResultAndGetDefault2FAScreen(
                                        credentials));

        // then
        verifyThatFromUsersPerspectiveThrowableIsTheSameAsGivenAgentException(
                throwable, expectedException);
        mocksToVerifyInOrder
                .verify(driverWrapper)
                .searchForFirstElement(
                        ElementsSearchQuery.builder()
                                .searchInParentWindow(NOT_EMPTY_NEMID_TOKEN)
                                .searchInAnIframe(ELEMENTS_TO_SEARCH_FOR_IN_IFRAME)
                                .searchForSeconds(30)
                                .build());
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @SuppressWarnings({"unused", "unchecked"})
    private static Object[] errorHeadingTextsWithExpectedException() {
        List<ErrorTextTestParams> knownErrorCases =
                asList(
                                ErrorTextTestParams.of(
                                        "use new code card",
                                        NemIdError.USE_NEW_CODE_CARD.exception()),
                                ErrorTextTestParams.of(
                                        "nemid revoked", NemIdError.NEMID_BLOCKED.exception()))
                        .stream()
                        .map(
                                errorTextTestParams ->
                                        asList(
                                                errorTextTestParams,
                                                errorTextTestParams.modifyErrorText(
                                                        String::toUpperCase),
                                                errorTextTestParams.modifyErrorText(
                                                        StringUtils::capitalize),
                                                errorTextTestParams.changeErrorTextCase(
                                                        CaseFormat.LOWER_CAMEL),
                                                errorTextTestParams.changeErrorTextCase(
                                                        CaseFormat.UPPER_CAMEL),
                                                errorTextTestParams.addErrorTextSuffix(
                                                        "!@$#%$^%  &^%*")))
                        .flatMap(List::stream)
                        .collect(Collectors.toList());

        List<ErrorTextTestParams> unknownErrorCases =
                knownErrorCases.stream()
                        .map(
                                errorTextTestParams ->
                                        asList(
                                                errorTextTestParams.addErrorTextPrefix("@#%123"),
                                                errorTextTestParams.changeErrorTextCompletely(""),
                                                errorTextTestParams.changeErrorTextCompletely(" "),
                                                errorTextTestParams.changeErrorTextCompletely(
                                                        "some unknown text")))
                        .flatMap(List::stream)
                        .map(
                                errorTextTestParams ->
                                        errorTextTestParams.changeExpectedError(
                                                LoginError.CREDENTIALS_VERIFICATION_ERROR
                                                        .exception()))
                        .collect(Collectors.toList());

        return joinLists(knownErrorCases, unknownErrorCases).stream()
                .map(ErrorTextTestParams::toTestParameters)
                .toArray();
    }

    @Test
    public void should_fail_when_there_is_a_nem_id_token() {
        // given
        WebElement tokenElement = webElementMockWithText("--- SAMPLE TOKEN ---");
        when(driverWrapper.searchForFirstElement(any()))
                .thenReturn(ElementsSearchResult.of(NOT_EMPTY_NEMID_TOKEN, tokenElement));

        Throwable tokenValidationError = new RuntimeException("token validation error");
        doThrow(tokenValidationError)
                .when(tokenValidator)
                .throwInvalidTokenExceptionWithoutValidation(any());

        // when
        Throwable throwable =
                catchThrowable(
                        () ->
                                verifyLoginResponseStep.checkLoginResultAndGetDefault2FAScreen(
                                        credentials));

        // then
        assertThat(throwable).isEqualTo(tokenValidationError);
        mocksToVerifyInOrder
                .verify(driverWrapper)
                .searchForFirstElement(
                        ElementsSearchQuery.builder()
                                .searchInParentWindow(NOT_EMPTY_NEMID_TOKEN)
                                .searchInAnIframe(ELEMENTS_TO_SEARCH_FOR_IN_IFRAME)
                                .searchForSeconds(30)
                                .build());
        mocksToVerifyInOrder
                .verify(tokenValidator)
                .throwInvalidTokenExceptionWithoutValidation("--- SAMPLE TOKEN ---");
    }

    @Test
    public void should_search_credentials_validation_elements_for_30_seconds_and_then_fail() {
        // given
        when(driverWrapper.searchForFirstElement(any())).thenReturn(ElementsSearchResult.empty());

        // when
        Throwable throwable =
                catchThrowable(
                        () ->
                                verifyLoginResponseStep.checkLoginResultAndGetDefault2FAScreen(
                                        credentials));

        // then
        verifyThatFromUsersPerspectiveThrowableIsTheSameAsGivenAgentException(
                throwable, LoginError.CREDENTIALS_VERIFICATION_ERROR.exception());

        mocksToVerifyInOrder
                .verify(driverWrapper)
                .searchForFirstElement(
                        ElementsSearchQuery.builder()
                                .searchInParentWindow(NOT_EMPTY_NEMID_TOKEN)
                                .searchInAnIframe(ELEMENTS_TO_SEARCH_FOR_IN_IFRAME)
                                .searchForSeconds(30)
                                .build());
    }
}
