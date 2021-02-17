package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.codetoken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.NEMID_CODE_TOKEN_SERIAL_NUMBER;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.util.NemIdTestHelper.nemIdMetricsMock;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.util.NemIdTestHelper.verifyThatFromUsersPerspectiveThrowableIsTheSameAsGivenAgentException;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.util.NemIdTestHelper.webElementMockWithText;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lombok.Builder;
import lombok.Data;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.utils.supplementalfields.CommonFields;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdCredentialsStatusUpdater;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.utils.NemIdWebDriverWrapper;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.NemIdCodeAppConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.exception.NemIdError;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableKey;

@RunWith(JUnitParamsRunner.class)
public class NemIdCodeTokenAskUserForCodeStepTest {

    private NemIdWebDriverWrapper driverWrapper;
    private Catalog catalog;
    private NemIdCredentialsStatusUpdater statusUpdater;
    private SupplementalInformationController supplementalInformationController;

    private Credentials credentials;
    private NemIdCodeTokenAskUserForCodeStep askUserForCodeStep;

    private InOrder mocksToVerifyInOrder;

    private static final String VALID_TOKEN_SERIAL_NUMBER = "1234 1234 1234 1234";
    private static final String VALID_CODE = "123456";

    private static final List<String> VALID_TOKEN_SERIAL_NUMBERS =
            Arrays.asList(VALID_TOKEN_SERIAL_NUMBER, "0000 0000 0000 0000");
    private static final List<String> INVALID_TOKEN_SERIAL_NUMBERS =
            Arrays.asList(
                    "12345 1234 1234 1234",
                    "123 1234 1234 1234",
                    "-1234 1234 1234 1234",
                    "A123-123-1234",
                    "A123-123-123-",
                    "-A123-123-123",
                    "A123123-123",
                    "A123123123");
    private static final List<String> VALID_CODES = Arrays.asList(VALID_CODE, "111111");
    private static final List<String> INVALID_CODES = Arrays.asList("12345", "123456a", "-123456");

    @Before
    public void setUp() {
        driverWrapper = mock(NemIdWebDriverWrapper.class);

        catalog = mock(Catalog.class);
        when(catalog.getString(any(LocalizableKey.class))).thenReturn("something not empty");

        statusUpdater = mock(NemIdCredentialsStatusUpdater.class);
        supplementalInformationController = mock(SupplementalInformationController.class);

        askUserForCodeStep =
                new NemIdCodeTokenAskUserForCodeStep(
                        driverWrapper,
                        nemIdMetricsMock(),
                        catalog,
                        statusUpdater,
                        supplementalInformationController);

        credentials = mock(Credentials.class);

        mocksToVerifyInOrder =
                inOrder(driverWrapper, statusUpdater, supplementalInformationController);
    }

    @Test
    @Parameters(method = "validCodeTokenNumbers")
    public void should_return_code_provided_by_user_when_all_numbers_are_valid(
            CodeTokenNumbers numbers) {
        // given
        String validTokenSerialNumber = numbers.getTokenSerialNumber();
        String validCode = numbers.getCode();

        mockThereIsWebElementWithText(NEMID_CODE_TOKEN_SERIAL_NUMBER, validTokenSerialNumber);

        mockSupplementalInfoResponse(
                ImmutableMap.of(
                        CommonFields.CodeTokenInfo.FIELD_KEY, validTokenSerialNumber,
                        CommonFields.CodeTokenCode.FIELD_KEY, validCode));

        // when
        String code = askUserForCodeStep.askForCodeAndValidateResponse(credentials);

        // then
        assertThat(code).isEqualTo(validCode);

        mocksToVerifyInOrder.verify(driverWrapper).tryFindElement(NEMID_CODE_TOKEN_SERIAL_NUMBER);
        mocksToVerifyInOrder
                .verify(statusUpdater)
                .updateStatusPayload(
                        credentials, NemIdCodeAppConstants.UserMessage.PROVIDE_CODE_TOKEN_CODE);

        ArgumentCaptor<Field> argumentCaptor = ArgumentCaptor.forClass(Field.class);
        mocksToVerifyInOrder
                .verify(supplementalInformationController)
                .askSupplementalInformationSync(argumentCaptor.capture());
        Field field1 = argumentCaptor.getAllValues().get(0);
        Field field2 = argumentCaptor.getAllValues().get(1);
        assertThat(field1)
                .isEqualTo(CommonFields.CodeTokenInfo.build(catalog, validTokenSerialNumber));
        assertThat(field2).isEqualTo(CommonFields.CodeTokenCode.build(catalog));

        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @SuppressWarnings("unused")
    private Object[] validCodeTokenNumbers() {
        List<CodeTokenNumbers> numbers = new ArrayList<>();
        for (String serialNumber : VALID_TOKEN_SERIAL_NUMBERS) {
            for (String code : VALID_CODES) {
                numbers.add(
                        CodeTokenNumbers.builder()
                                .tokenSerialNumber(serialNumber)
                                .code(code)
                                .build());
            }
        }
        return numbers.toArray(new Object[0]);
    }

    @Test
    public void
            should_throw_invalid_state_exception_when_there_is_no_token_serial_number_on_screen() {
        // given
        mockThereIsNoSuchElement(NEMID_CODE_TOKEN_SERIAL_NUMBER);

        // when
        Throwable throwable =
                catchThrowable(() -> askUserForCodeStep.askForCodeAndValidateResponse(credentials));

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot find NemId token serial number");

        mocksToVerifyInOrder.verify(driverWrapper).tryFindElement(NEMID_CODE_TOKEN_SERIAL_NUMBER);
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    @Parameters(method = "invalidTokenSerialNumbers")
    public void
            should_throw_invalid_state_exception_when_serial_number_extracted_from_screen_is_invalid(
                    String invalidTokenSerialNumber) {
        // given
        mockThereIsWebElementWithText(NEMID_CODE_TOKEN_SERIAL_NUMBER, invalidTokenSerialNumber);

        // when
        Throwable throwable =
                catchThrowable(() -> askUserForCodeStep.askForCodeAndValidateResponse(credentials));

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        String.format(
                                "Invalid NemId code token serial number: \"%s\"",
                                invalidTokenSerialNumber));

        mocksToVerifyInOrder.verify(driverWrapper).tryFindElement(NEMID_CODE_TOKEN_SERIAL_NUMBER);
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    @Parameters(method = "invalidCodes")
    public void should_throw_invalid_code_token_code_exception_on_invalid_code_from_user(
            String invalidCode) {
        // given
        mockThereIsWebElementWithText(NEMID_CODE_TOKEN_SERIAL_NUMBER, VALID_TOKEN_SERIAL_NUMBER);

        mockSupplementalInfoResponse(
                ImmutableMap.of(
                        CommonFields.CodeTokenInfo.FIELD_KEY, VALID_TOKEN_SERIAL_NUMBER,
                        CommonFields.CodeTokenCode.FIELD_KEY, invalidCode));

        // when
        Throwable throwable =
                catchThrowable(() -> askUserForCodeStep.askForCodeAndValidateResponse(credentials));

        // then
        verifyThatFromUsersPerspectiveThrowableIsTheSameAsGivenAgentException(
                throwable, NemIdError.INVALID_CODE_TOKEN_CODE.exception());

        mocksToVerifyInOrder.verify(driverWrapper).tryFindElement(NEMID_CODE_TOKEN_SERIAL_NUMBER);
        mocksToVerifyInOrder
                .verify(statusUpdater)
                .updateStatusPayload(
                        credentials, NemIdCodeAppConstants.UserMessage.PROVIDE_CODE_TOKEN_CODE);

        ArgumentCaptor<Field> argumentCaptor = ArgumentCaptor.forClass(Field.class);
        mocksToVerifyInOrder
                .verify(supplementalInformationController)
                .askSupplementalInformationSync(argumentCaptor.capture());
        Field field1 = argumentCaptor.getAllValues().get(0);
        Field field2 = argumentCaptor.getAllValues().get(1);
        assertThat(field1)
                .isEqualTo(CommonFields.CodeTokenInfo.build(catalog, VALID_TOKEN_SERIAL_NUMBER));
        assertThat(field2).isEqualTo(CommonFields.CodeTokenCode.build(catalog));

        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @SuppressWarnings("unused")
    private Object[] invalidTokenSerialNumbers() {
        return INVALID_TOKEN_SERIAL_NUMBERS.toArray(new Object[0]);
    }

    @SuppressWarnings("unused")
    private Object[] invalidCodes() {
        return INVALID_CODES.toArray(new Object[0]);
    }

    @SuppressWarnings("SameParameterValue")
    private void mockThereIsWebElementWithText(By elementSelector, String text) {
        WebElement element = webElementMockWithText(text);
        when(driverWrapper.tryFindElement(elementSelector)).thenReturn(Optional.of(element));
    }

    @SuppressWarnings("SameParameterValue")
    private void mockThereIsNoSuchElement(By elementSelector) {
        when(driverWrapper.tryFindElement(elementSelector)).thenReturn(Optional.empty());
    }

    private void mockSupplementalInfoResponse(Map<String, String> response) {
        when(supplementalInformationController.askSupplementalInformationSync(any(), any()))
                .thenReturn(response);
    }

    @Data
    @Builder
    private static class CodeTokenNumbers {
        private String tokenSerialNumber;
        private String code;
    }
}
