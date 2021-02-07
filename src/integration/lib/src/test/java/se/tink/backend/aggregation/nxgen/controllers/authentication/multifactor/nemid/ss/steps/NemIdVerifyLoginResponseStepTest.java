package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.Errors.ENTER_ACTIVATION_PASSWORD;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.NEMID_BLOCKED;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.NEMID_CODE_APP_METHOD;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.NEMID_CODE_CARD_METHOD;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.NEMID_CODE_CARD_USE_NEW_CODE_CARD;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.NEMID_CODE_TOKEN_METHOD;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.NEMID_TOKEN;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.NOT_EMPTY_ERROR_MESSAGE;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.util.NemIdTestHelper.asArray;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.util.NemIdTestHelper.nemIdMetricsMock;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.util.NemIdTestHelper.verifyThatFromUsersPerspectiveThrowableIsTheSameAsGivenAgentException;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.util.NemIdTestHelper.webElementMock;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.util.NemIdTestHelper.webElementMockWithText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
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
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdCredentialsStatusUpdater;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdTokenValidator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdWebDriverWrapper;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdWebDriverWrapper.ElementsSearchResult;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.NemIdCodeAppConstants.UserMessage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.exception.NemIdError;
import se.tink.libraries.i18n.LocalizableKey;

@RunWith(JUnitParamsRunner.class)
public class NemIdVerifyLoginResponseStepTest {

    private NemIdWebDriverWrapper driverWrapper;
    private NemIdCredentialsStatusUpdater statusUpdater;
    private NemIdTokenValidator tokenValidator;

    private NemIdVerifyLoginResponseStep verifyLoginResponseStep;

    private Credentials credentials;
    private List<By> allWebElementsRelevantToCredentialsVerification;
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
        allWebElementsRelevantToCredentialsVerification =
                Arrays.asList(
                        NEMID_TOKEN,
                        NEMID_BLOCKED,
                        NEMID_CODE_APP_METHOD,
                        NEMID_CODE_CARD_METHOD,
                        NEMID_CODE_CARD_USE_NEW_CODE_CARD,
                        NEMID_CODE_TOKEN_METHOD,
                        NOT_EMPTY_ERROR_MESSAGE);
        mocksToVerifyInOrder = Mockito.inOrder(driverWrapper, tokenValidator, statusUpdater);
    }

    @Test
    public void should_update_status_payload_when_nem_id_is_suggesting_code_app_method() {
        // given
        setCredentialsValidationElementThatWillBeFound(NEMID_CODE_APP_METHOD, webElementMock());

        // when
        verifyLoginResponseStep.checkLoginResultAndGetAvailable2FAMethod(credentials);

        // then
        mocksToVerifyInOrder
                .verify(driverWrapper)
                .searchForFirstElement(allWebElementsRelevantToCredentialsVerification, 30);
        mocksToVerifyInOrder
                .verify(statusUpdater)
                .updateStatusPayload(credentials, UserMessage.VALID_CREDS);
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    @Parameters(method = "errorTextAndExpectedException")
    public void should_fail_when_there_is_a_not_empty_error_message(
            String errorText, AgentException expectedException) {
        // given
        setCredentialsValidationElementThatWillBeFound(
                NOT_EMPTY_ERROR_MESSAGE, webElementMockWithText(errorText));

        // when
        Throwable throwable =
                catchThrowable(
                        () ->
                                verifyLoginResponseStep.checkLoginResultAndGetAvailable2FAMethod(
                                        credentials));

        // then
        verifyThatFromUsersPerspectiveThrowableIsTheSameAsGivenAgentException(
                throwable, expectedException);
        mocksToVerifyInOrder
                .verify(driverWrapper)
                .searchForFirstElement(allWebElementsRelevantToCredentialsVerification, 30);
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @SuppressWarnings("unused")
    private static Object[] errorTextAndExpectedException() {
        List<Object[]> args = new ArrayList<>();

        args.add(asArray("incorrect user", LoginError.INCORRECT_CREDENTIALS.exception()));
        args.add(asArray("inCOrrect user!@#^#^", LoginError.INCORRECT_CREDENTIALS.exception()));
        args.add(asArray("incorrecT passworD", LoginError.INCORRECT_CREDENTIALS.exception()));
        args.add(asArray("incorrect pASsword!@#^#^", LoginError.INCORRECT_CREDENTIALS.exception()));

        args.add(
                asArray(
                        "-incorrect user!@#^#^",
                        LoginError.CREDENTIALS_VERIFICATION_ERROR.exception()));
        args.add(
                asArray(
                        "-incorrect password!@#^#^",
                        LoginError.CREDENTIALS_VERIFICATION_ERROR.exception()));

        args.add(asArray("fEjl i Bruger", LoginError.INCORRECT_CREDENTIALS.exception()));
        args.add(asArray("fejl i bRUger!@$#%$^#&%", LoginError.INCORRECT_CREDENTIALS.exception()));
        args.add(asArray("fejl i aDGangskode", LoginError.INCORRECT_CREDENTIALS.exception()));
        args.add(
                asArray(
                        "fejl i adgangskode!@$#%$^#&%",
                        LoginError.INCORRECT_CREDENTIALS.exception()));

        args.add(asArray("-fejl i bruger", LoginError.CREDENTIALS_VERIFICATION_ERROR.exception()));
        args.add(
                asArray(
                        "-fejl i adgangskode",
                        LoginError.CREDENTIALS_VERIFICATION_ERROR.exception()));

        args.add(asArray("Indtast bruger", LoginError.INCORRECT_CREDENTIALS.exception()));
        args.add(asArray("indtast bRugeR!@$#%$^#&%", LoginError.INCORRECT_CREDENTIALS.exception()));
        args.add(asArray("indtast adgangskode", LoginError.INCORRECT_CREDENTIALS.exception()));
        args.add(
                asArray(
                        "indtast adgangskode!@$#%$^#&%",
                        LoginError.INCORRECT_CREDENTIALS.exception()));

        args.add(asArray("-indtast bruger", LoginError.CREDENTIALS_VERIFICATION_ERROR.exception()));
        args.add(
                asArray(
                        "-indtast adgangskode",
                        LoginError.CREDENTIALS_VERIFICATION_ERROR.exception()));

        args.add(
                asArray(
                        "Enter activAtIoN password.",
                        LoginError.INCORRECT_CREDENTIALS.exception(
                                new LocalizableKey(ENTER_ACTIVATION_PASSWORD))));
        args.add(
                asArray(
                        "enter activation paSSworD.",
                        LoginError.INCORRECT_CREDENTIALS.exception(
                                new LocalizableKey(ENTER_ACTIVATION_PASSWORD))));

        args.add(asArray(" ", LoginError.CREDENTIALS_VERIFICATION_ERROR.exception()));
        args.add(asArray("sadfsdg", LoginError.CREDENTIALS_VERIFICATION_ERROR.exception()));

        return args.toArray(new Object[0]);
    }

    @Test
    public void should_fail_when_there_is_a_nem_id_token() {
        // given
        setCredentialsValidationElementThatWillBeFound(
                NEMID_TOKEN, webElementMockWithText("--- SAMPLE TOKEN ---"));

        Throwable tokenValidationError = new RuntimeException("token validation error");
        doThrow(tokenValidationError)
                .when(tokenValidator)
                .throwInvalidTokenExceptionWithoutValidation(any());

        // when
        Throwable throwable =
                catchThrowable(
                        () ->
                                verifyLoginResponseStep.checkLoginResultAndGetAvailable2FAMethod(
                                        credentials));

        // then
        assertThat(throwable).isEqualTo(tokenValidationError);
        mocksToVerifyInOrder
                .verify(driverWrapper)
                .searchForFirstElement(allWebElementsRelevantToCredentialsVerification, 30);
        mocksToVerifyInOrder
                .verify(tokenValidator)
                .throwInvalidTokenExceptionWithoutValidation("--- SAMPLE TOKEN ---");
    }

    @Test
    public void should_throw_use_new_code_card_exception() {
        // given
        setCredentialsValidationElementThatWillBeFound(
                NEMID_CODE_CARD_USE_NEW_CODE_CARD, webElementMock());

        // when
        Throwable throwable =
                catchThrowable(
                        () ->
                                verifyLoginResponseStep.checkLoginResultAndGetAvailable2FAMethod(
                                        credentials));

        // then
        verifyThatFromUsersPerspectiveThrowableIsTheSameAsGivenAgentException(
                throwable, NemIdError.USE_NEW_CODE_CARD.exception());

        mocksToVerifyInOrder
                .verify(driverWrapper)
                .searchForFirstElement(allWebElementsRelevantToCredentialsVerification, 30);
    }

    @Test
    public void should_throw_nem_id_blocked_exception() {
        // given
        setCredentialsValidationElementThatWillBeFound(NEMID_BLOCKED, webElementMock());

        // when
        Throwable throwable =
                catchThrowable(
                        () ->
                                verifyLoginResponseStep.checkLoginResultAndGetAvailable2FAMethod(
                                        credentials));

        // then
        verifyThatFromUsersPerspectiveThrowableIsTheSameAsGivenAgentException(
                throwable, NemIdError.NEMID_BLOCKED.exception());

        mocksToVerifyInOrder
                .verify(driverWrapper)
                .searchForFirstElement(allWebElementsRelevantToCredentialsVerification, 30);
    }

    @Test
    public void should_search_credentials_validation_elements_for_30_seconds_and_then_fail() {
        // given
        setEmptyCredentialsVerificationElementsSearchResult();

        // when
        Throwable throwable =
                catchThrowable(
                        () ->
                                verifyLoginResponseStep.checkLoginResultAndGetAvailable2FAMethod(
                                        credentials));

        // then
        verifyThatFromUsersPerspectiveThrowableIsTheSameAsGivenAgentException(
                throwable, LoginError.CREDENTIALS_VERIFICATION_ERROR.exception());

        mocksToVerifyInOrder
                .verify(driverWrapper)
                .searchForFirstElement(allWebElementsRelevantToCredentialsVerification, 30);
    }

    private void setCredentialsValidationElementThatWillBeFound(By by, WebElement webElement) {
        ElementsSearchResult findFirstElementResult = ElementsSearchResult.of(by, webElement);
        when(driverWrapper.searchForFirstElement(any(), anyInt()))
                .thenReturn(findFirstElementResult);
    }

    private void setEmptyCredentialsVerificationElementsSearchResult() {
        when(driverWrapper.searchForFirstElement(any(), anyInt()))
                .thenReturn(ElementsSearchResult.empty());
    }
}
