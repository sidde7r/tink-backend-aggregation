package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.codecard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.NEMID_CODE_CARD_CODE_NUMBER;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.NEMID_CODE_CARD_NUMBER;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.codecard.NemIdCodeCardAskUserForCodeStep.EXPECTED_CODE_LENGTH;
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
import se.tink.backend.aggregation.agents.exceptions.nemid.NemIdError;
import se.tink.backend.aggregation.agents.utils.supplementalfields.CommonFields;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdCredentialsStatusUpdater;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.utils.NemIdWebDriverWrapper;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.NemIdCodeAppConstants;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableKey;

@RunWith(JUnitParamsRunner.class)
public class NemIdCodeCardAskUserForCodeStepTest {

    private NemIdWebDriverWrapper driverWrapper;
    private Catalog catalog;
    private NemIdCredentialsStatusUpdater statusUpdater;
    private SupplementalInformationController supplementalInformationController;

    private Credentials credentials;
    private NemIdCodeCardAskUserForCodeStep askUserForCodeStep;

    private InOrder mocksToVerifyInOrder;

    private static final String VALID_CARD_NUMBER = "A123-123-123";
    private static final String VALID_CODE_NUMBER = "1234";
    private static final String VALID_CODE = "123456";

    private static final List<String> VALID_CARD_NUMBERS =
            Arrays.asList(VALID_CARD_NUMBER, "B000-000-000");
    private static final List<String> INVALID_CARD_NUMBERS =
            Arrays.asList(
                    "123-456-789",
                    "A1234-123-123",
                    "A123-1234-123",
                    "A123-123-1234",
                    "A123-123-123-",
                    "-A123-123-123",
                    "A123123-123",
                    "A123123123");
    private static final List<String> VALID_CODE_NUMBERS = Arrays.asList(VALID_CODE_NUMBER, "9999");
    private static final List<String> INVALID_CODE_NUMBERS =
            Arrays.asList("123", "12345", "1234a", "-1234");
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
                new NemIdCodeCardAskUserForCodeStep(
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
    @Parameters(method = "validCodeCardNumbers")
    public void should_return_code_provided_by_user_when_all_numbers_are_valid(
            CodeCardNumbers numbers) {
        // given
        String validCardNumber = numbers.getCardNumber();
        String validCodeNumber = numbers.getCodeNumber();
        String validCode = numbers.getCode();

        mockThereIsWebElementWithText(NEMID_CODE_CARD_NUMBER, validCardNumber);
        mockThereIsWebElementWithText(NEMID_CODE_CARD_CODE_NUMBER, validCodeNumber);

        Map<String, String> supplementalInfoResponse =
                ImmutableMap.of(
                        CommonFields.KeyCardInfo.FIELD_KEY, validCodeNumber,
                        CommonFields.KeyCardCode.FIELD_KEY, validCode);
        when(supplementalInformationController.askSupplementalInformationSync(any(), any()))
                .thenReturn(supplementalInfoResponse);

        // when
        String code = askUserForCodeStep.askForCodeAndValidateResponse(credentials);

        // then
        assertThat(code).isEqualTo(validCode);

        mocksToVerifyInOrder.verify(driverWrapper).tryFindElement(NEMID_CODE_CARD_NUMBER);
        mocksToVerifyInOrder.verify(driverWrapper).tryFindElement(NEMID_CODE_CARD_CODE_NUMBER);
        mocksToVerifyInOrder
                .verify(statusUpdater)
                .updateStatusPayload(
                        credentials, NemIdCodeAppConstants.UserMessage.PROVIDE_CODE_CARD_CODE);

        ArgumentCaptor<Field> argumentCaptor = ArgumentCaptor.forClass(Field.class);
        mocksToVerifyInOrder
                .verify(supplementalInformationController)
                .askSupplementalInformationSync(argumentCaptor.capture());
        Field field1 = argumentCaptor.getAllValues().get(0);
        Field field2 = argumentCaptor.getAllValues().get(1);
        assertThat(field1)
                .isEqualTo(
                        CommonFields.KeyCardInfo.build(catalog, validCodeNumber, validCardNumber));
        assertThat(field2).isEqualTo(CommonFields.KeyCardCode.build(catalog, EXPECTED_CODE_LENGTH));

        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @SuppressWarnings("unused")
    private Object[] validCodeCardNumbers() {
        List<CodeCardNumbers> numbers = new ArrayList<>();
        for (String cardNumber : VALID_CARD_NUMBERS) {
            for (String codeNumber : VALID_CODE_NUMBERS) {
                for (String code : VALID_CODES) {
                    numbers.add(
                            CodeCardNumbers.builder()
                                    .cardNumber(cardNumber)
                                    .codeNumber(codeNumber)
                                    .code(code)
                                    .build());
                }
            }
        }
        return numbers.toArray(new Object[0]);
    }

    @Test
    public void should_throw_invalid_state_exception_when_there_is_no_card_number_on_screen() {
        // given
        mockThereIsNoSuchElement(NEMID_CODE_CARD_NUMBER);

        // when
        Throwable throwable =
                catchThrowable(() -> askUserForCodeStep.askForCodeAndValidateResponse(credentials));

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot find NemId card number");

        mocksToVerifyInOrder.verify(driverWrapper).tryFindElement(NEMID_CODE_CARD_NUMBER);
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void should_throw_invalid_state_exception_when_there_is_no_code_number_on_screen() {
        // given
        mockThereIsWebElementWithText(NEMID_CODE_CARD_NUMBER, VALID_CARD_NUMBER);
        mockThereIsNoSuchElement(NEMID_CODE_CARD_CODE_NUMBER);

        // when
        Throwable throwable =
                catchThrowable(() -> askUserForCodeStep.askForCodeAndValidateResponse(credentials));

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot find NemId code card code number");

        mocksToVerifyInOrder.verify(driverWrapper).tryFindElement(NEMID_CODE_CARD_NUMBER);
        mocksToVerifyInOrder.verify(driverWrapper).tryFindElement(NEMID_CODE_CARD_CODE_NUMBER);
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    @Parameters(method = "invalidCodeCardNumbers")
    public void
            should_throw_invalid_state_exception_when_card_number_extracted_from_screen_is_invalid(
                    String invalidCodeCardNumber) {
        // given
        mockThereIsWebElementWithText(NEMID_CODE_CARD_NUMBER, invalidCodeCardNumber);

        // when
        Throwable throwable =
                catchThrowable(() -> askUserForCodeStep.askForCodeAndValidateResponse(credentials));

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        String.format(
                                "Invalid NemId code card number: \"%s\"", invalidCodeCardNumber));

        mocksToVerifyInOrder.verify(driverWrapper).tryFindElement(NEMID_CODE_CARD_NUMBER);
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    @Parameters(method = "invalidCodeNumbers")
    public void
            should_throw_invalid_state_exception_when_code_number_extracted_from_screen_is_invalid(
                    String invalidCodeNumber) {
        // given
        mockThereIsWebElementWithText(NEMID_CODE_CARD_NUMBER, VALID_CARD_NUMBER);
        mockThereIsWebElementWithText(NEMID_CODE_CARD_CODE_NUMBER, invalidCodeNumber);

        // when
        Throwable throwable =
                catchThrowable(() -> askUserForCodeStep.askForCodeAndValidateResponse(credentials));

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(String.format("Invalid NemId code number: \"%s\"", invalidCodeNumber));

        mocksToVerifyInOrder.verify(driverWrapper).tryFindElement(NEMID_CODE_CARD_NUMBER);
        mocksToVerifyInOrder.verify(driverWrapper).tryFindElement(NEMID_CODE_CARD_CODE_NUMBER);
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    @Parameters(method = "invalidCodes")
    public void should_throw_invalid_code_card_code_exception_on_invalid_code_from_user(
            String invalidCode) {
        // given
        mockThereIsWebElementWithText(NEMID_CODE_CARD_NUMBER, VALID_CARD_NUMBER);
        mockThereIsWebElementWithText(NEMID_CODE_CARD_CODE_NUMBER, VALID_CODE_NUMBER);

        Map<String, String> supplementalInfoResponse =
                ImmutableMap.of(
                        CommonFields.KeyCardInfo.FIELD_KEY, VALID_CODE_NUMBER,
                        CommonFields.KeyCardCode.FIELD_KEY, invalidCode);
        when(supplementalInformationController.askSupplementalInformationSync(any(), any()))
                .thenReturn(supplementalInfoResponse);

        // when
        Throwable throwable =
                catchThrowable(() -> askUserForCodeStep.askForCodeAndValidateResponse(credentials));

        // then
        verifyThatFromUsersPerspectiveThrowableIsTheSameAsGivenAgentException(
                throwable, NemIdError.INVALID_CODE_CARD_CODE.exception());

        mocksToVerifyInOrder.verify(driverWrapper).tryFindElement(NEMID_CODE_CARD_NUMBER);
        mocksToVerifyInOrder.verify(driverWrapper).tryFindElement(NEMID_CODE_CARD_CODE_NUMBER);
        mocksToVerifyInOrder
                .verify(statusUpdater)
                .updateStatusPayload(
                        credentials, NemIdCodeAppConstants.UserMessage.PROVIDE_CODE_CARD_CODE);

        ArgumentCaptor<Field> argumentCaptor = ArgumentCaptor.forClass(Field.class);
        mocksToVerifyInOrder
                .verify(supplementalInformationController)
                .askSupplementalInformationSync(argumentCaptor.capture());
        Field field1 = argumentCaptor.getAllValues().get(0);
        Field field2 = argumentCaptor.getAllValues().get(1);
        assertThat(field1)
                .isEqualTo(
                        CommonFields.KeyCardInfo.build(
                                catalog, VALID_CODE_NUMBER, VALID_CARD_NUMBER));
        assertThat(field2).isEqualTo(CommonFields.KeyCardCode.build(catalog, EXPECTED_CODE_LENGTH));

        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @SuppressWarnings("unused")
    private Object[] invalidCodeCardNumbers() {
        return INVALID_CARD_NUMBERS.toArray(new Object[0]);
    }

    @SuppressWarnings("unused")
    private Object[] invalidCodeNumbers() {
        return INVALID_CODE_NUMBERS.toArray(new Object[0]);
    }

    @SuppressWarnings("unused")
    private Object[] invalidCodes() {
        return INVALID_CODES.toArray(new Object[0]);
    }

    private void mockThereIsWebElementWithText(By elementSelector, String text) {
        WebElement element = webElementMockWithText(text);
        when(driverWrapper.tryFindElement(elementSelector)).thenReturn(Optional.of(element));
    }

    private void mockThereIsNoSuchElement(By elementSelector) {
        when(driverWrapper.tryFindElement(elementSelector)).thenReturn(Optional.empty());
    }

    @Data
    @Builder
    private static class CodeCardNumbers {
        private String cardNumber;
        private String codeNumber;
        private String code;
    }
}
