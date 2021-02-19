package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.codetoken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.NEMID_CODE_TOKEN_INPUT;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.NOT_EMPTY_ERROR_MESSAGE;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.NOT_EMPTY_NEMID_TOKEN;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.SUBMIT_BUTTON;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.util.NemIdTestHelper.asList;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.util.NemIdTestHelper.joinLists;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.util.NemIdTestHelper.nemIdMetricsMock;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.util.NemIdTestHelper.verifyThatFromUsersPerspectiveThrowableIsTheSameAsGivenAgentException;
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
import org.openqa.selenium.WebElement;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.utils.ElementsSearchQuery;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.utils.ElementsSearchResult;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.utils.NemIdWebDriverWrapper;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.util.ErrorTextTestParams;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.exception.NemIdError;

@RunWith(JUnitParamsRunner.class)
public class NemIdCodeTokenGetTokenStepTest {

    private static final String CODE_TOKEN_CODE = "SAMPLE CODE CARD CODE";

    private NemIdWebDriverWrapper driverWrapper;
    private InOrder mocksToVerifyInOrder;

    private NemIdCodeTokenGetTokenStep getTokenStep;

    @Before
    public void setUp() {
        driverWrapper = mock(NemIdWebDriverWrapper.class);
        mocksToVerifyInOrder = Mockito.inOrder(driverWrapper);

        getTokenStep = new NemIdCodeTokenGetTokenStep(driverWrapper, nemIdMetricsMock());
    }

    @Test
    public void should_submit_code_from_user_and_read_nem_id_token() {
        // given
        WebElement element = webElementMockWithText("--- SAMPLE TOKEN ---");
        when(driverWrapper.searchForFirstElement(any()))
                .thenReturn(ElementsSearchResult.of(NOT_EMPTY_NEMID_TOKEN, element));

        // when
        String nemIdToken = getTokenStep.enterCodeAndGetToken(CODE_TOKEN_CODE);

        // then
        assertThat(nemIdToken).isEqualTo("--- SAMPLE TOKEN ---");

        mocksToVerifyInOrder
                .verify(driverWrapper)
                .setValueToElement(CODE_TOKEN_CODE, NEMID_CODE_TOKEN_INPUT);
        mocksToVerifyInOrder.verify(driverWrapper).clickButton(SUBMIT_BUTTON);
        mocksToVerifyInOrder
                .verify(driverWrapper)
                .searchForFirstElement(
                        ElementsSearchQuery.builder()
                                .searchInParentWindow(NOT_EMPTY_NEMID_TOKEN)
                                .searchInAnIframe(NOT_EMPTY_ERROR_MESSAGE)
                                .build());
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    @Parameters(method = "errorMessageTextsWithExpectedException")
    public void should_throw_exception_on_not_empty_error_message(
            String errorMessage, AgentException expectedException) {
        // given
        WebElement element = webElementMockWithText(errorMessage);
        when(driverWrapper.searchForFirstElement(any()))
                .thenReturn(ElementsSearchResult.of(NOT_EMPTY_ERROR_MESSAGE, element));

        // when
        Throwable throwable =
                catchThrowable(() -> getTokenStep.enterCodeAndGetToken(CODE_TOKEN_CODE));

        // then
        verifyThatFromUsersPerspectiveThrowableIsTheSameAsGivenAgentException(
                throwable, expectedException);

        mocksToVerifyInOrder
                .verify(driverWrapper)
                .setValueToElement(CODE_TOKEN_CODE, NEMID_CODE_TOKEN_INPUT);
        mocksToVerifyInOrder.verify(driverWrapper).clickButton(SUBMIT_BUTTON);
        mocksToVerifyInOrder
                .verify(driverWrapper)
                .searchForFirstElement(
                        ElementsSearchQuery.builder()
                                .searchInParentWindow(NOT_EMPTY_NEMID_TOKEN)
                                .searchInAnIframe(NOT_EMPTY_ERROR_MESSAGE)
                                .build());
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @SuppressWarnings({"unused", "unchecked"})
    private static Object[] errorMessageTextsWithExpectedException() {
        List<ErrorTextTestParams> knownErrorCases =
                asList(
                                ErrorTextTestParams.of(
                                        "incorrect code",
                                        NemIdError.INVALID_CODE_TOKEN_CODE.exception()),
                                ErrorTextTestParams.of(
                                        "fejl i nøgle",
                                        NemIdError.INVALID_CODE_TOKEN_CODE.exception()))
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
                                                        "some unknown message")))
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
    public void should_try_to_find_token_or_error_message_for_10_seconds_and_then_fail() {
        // given
        when(driverWrapper.searchForFirstElement(any())).thenReturn(ElementsSearchResult.empty());

        // when
        Throwable throwable =
                catchThrowable(() -> getTokenStep.enterCodeAndGetToken(CODE_TOKEN_CODE));

        // then
        verifyThatFromUsersPerspectiveThrowableIsTheSameAsGivenAgentException(
                throwable, LoginError.CREDENTIALS_VERIFICATION_ERROR.exception());

        mocksToVerifyInOrder
                .verify(driverWrapper)
                .setValueToElement(CODE_TOKEN_CODE, NEMID_CODE_TOKEN_INPUT);
        mocksToVerifyInOrder.verify(driverWrapper).clickButton(SUBMIT_BUTTON);
        mocksToVerifyInOrder
                .verify(driverWrapper)
                .searchForFirstElement(
                        ElementsSearchQuery.builder()
                                .searchInParentWindow(NOT_EMPTY_NEMID_TOKEN)
                                .searchInAnIframe(NOT_EMPTY_ERROR_MESSAGE)
                                .searchForSeconds(10)
                                .build());
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }
}
