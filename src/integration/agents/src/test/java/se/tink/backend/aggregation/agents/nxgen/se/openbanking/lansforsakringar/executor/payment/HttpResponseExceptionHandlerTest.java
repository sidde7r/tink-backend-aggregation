package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment;

import static org.assertj.core.api.Assertions.catchThrowable;
import static se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException.TEMPORARILY_UNAVAILABLE_MESSAGE;
import static se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.ErrorMessages.INVALID_CREDITOR_ACCOUNT;
import static se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.ErrorMessages.INVALID_INFO_STRUCTURED;
import static se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.ErrorMessages.INVALID_INFO_UNSTRUCTURED;
import static se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.ErrorMessages.INVALID_REQUESTED_EXECUTION_DATE;
import static se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.ErrorMessages.NOT_ENOUGH_FUNDS;
import static se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.ErrorMessages.REMITTANCE_INFO_NOT_SET_FOR_GIROS;
import static se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.ErrorMessages.REQUESTED_DATE_CAN_NOT_BE_IN_THE_PAST;
import static se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.ErrorMessages.SERVICE_BLOCKED;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.payment.CreditorValidationException;
import se.tink.backend.aggregation.agents.exceptions.payment.DateValidationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.exceptions.payment.ReferenceValidationException;

public class HttpResponseExceptionHandlerTest {

    @Test
    public void testIsInvalidInfoStructured() {
        String errorMessage = getErrorMessage(INVALID_INFO_STRUCTURED);

        Throwable thrown =
                catchThrowable(() -> HttpResponseExceptionHandler.checkForErrors(errorMessage));

        Assertions.assertThat(thrown).isInstanceOf(ReferenceValidationException.class);
        Assertions.assertThat(thrown).hasMessage(INVALID_INFO_STRUCTURED);
    }

    @Test
    public void testIsInvalidInfoUnstructured() {
        String errorMessage = getErrorMessage(INVALID_INFO_UNSTRUCTURED);

        Throwable thrown =
                catchThrowable(() -> HttpResponseExceptionHandler.checkForErrors(errorMessage));

        Assertions.assertThat(thrown).isInstanceOf(ReferenceValidationException.class);
        Assertions.assertThat(thrown).hasMessage(INVALID_INFO_UNSTRUCTURED);
    }

    @Test
    public void testIsRemittanceInfoSetForGirosPayment() {
        String errorMessage = getErrorMessage(REMITTANCE_INFO_NOT_SET_FOR_GIROS);

        Throwable thrown =
                catchThrowable(() -> HttpResponseExceptionHandler.checkForErrors(errorMessage));

        Assertions.assertThat(thrown).isInstanceOf(ReferenceValidationException.class);
        Assertions.assertThat(thrown).hasMessage(REMITTANCE_INFO_NOT_SET_FOR_GIROS);
    }

    @Test
    public void testIsServiceBlocked() {
        String errorMessage = getErrorMessage(SERVICE_BLOCKED);

        Throwable thrown =
                catchThrowable(() -> HttpResponseExceptionHandler.checkForErrors(errorMessage));

        Assertions.assertThat(thrown).isInstanceOf(PaymentRejectedException.class);
        Assertions.assertThat(thrown).hasMessage(TEMPORARILY_UNAVAILABLE_MESSAGE);
    }

    @Test
    public void testIsInvalidCreditorAccount() {
        String errorMessage = getErrorMessage(INVALID_CREDITOR_ACCOUNT);

        Throwable thrown =
                catchThrowable(() -> HttpResponseExceptionHandler.checkForErrors(errorMessage));

        Assertions.assertThat(thrown).isInstanceOf(CreditorValidationException.class);
        Assertions.assertThat(thrown).hasMessage(INVALID_CREDITOR_ACCOUNT);
    }

    @Test
    public void testIsInvalidRequestedExecutionDate() {
        String errorMessage = getErrorMessage(INVALID_REQUESTED_EXECUTION_DATE);

        Throwable thrown =
                catchThrowable(() -> HttpResponseExceptionHandler.checkForErrors(errorMessage));

        Assertions.assertThat(thrown).isInstanceOf(DateValidationException.class);
        Assertions.assertThat(thrown).hasMessage(REQUESTED_DATE_CAN_NOT_BE_IN_THE_PAST);
    }

    @Test
    public void testIsNotEnoughFundsToMakePayment() {
        String errorMessage = getErrorMessage(NOT_ENOUGH_FUNDS);

        Throwable thrown =
                catchThrowable(() -> HttpResponseExceptionHandler.checkForErrors(errorMessage));

        Assertions.assertThat(thrown).isInstanceOf(DateValidationException.class);
        Assertions.assertThat(thrown).hasMessage(NOT_ENOUGH_FUNDS);
    }

    @Test
    public void testFormatErrorMessage() {
        String errorMessage = getErrorMessage("\"Other error\"");

        Throwable thrown =
                catchThrowable(() -> HttpResponseExceptionHandler.checkForErrors(errorMessage));

        Assertions.assertThat(thrown).isInstanceOf(PaymentRejectedException.class);
        Assertions.assertThat(thrown).hasMessage("Other error");
    }

    private String getErrorMessage(String error) {
        return "Response statusCode: 400 with body: {\"tppMessages\":[{\"code\":\"FORMAT_ERROR\","
                + "\"text\":"
                + error
                + ",\"category\":\"ERROR\"}]}";
    }
}
