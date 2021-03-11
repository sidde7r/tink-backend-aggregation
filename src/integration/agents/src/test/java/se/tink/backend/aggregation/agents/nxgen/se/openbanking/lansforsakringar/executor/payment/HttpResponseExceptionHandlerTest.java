package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment;

import static org.assertj.core.api.Assertions.catchThrowable;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.payment.CreditorValidationException;
import se.tink.backend.aggregation.agents.exceptions.payment.DateValidationException;
import se.tink.backend.aggregation.agents.exceptions.payment.InsufficientFundsException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.exceptions.payment.ReferenceValidationException;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException.EndUserMessage;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.ErrorMessages;

public class HttpResponseExceptionHandlerTest {

    @Test
    public void testIsInvalidInfoStructured() {
        String errorMessage = getErrorMessage(ErrorMessages.INVALID_INFO_STRUCTURED);

        Throwable thrown =
                catchThrowable(() -> HttpResponseExceptionHandler.checkForErrors(errorMessage));

        Assertions.assertThat(thrown).isInstanceOf(ReferenceValidationException.class);
        Assertions.assertThat(thrown).hasMessage(EndUserMessage.INVALID_OCR.getKey().get());
    }

    @Test
    public void testIsInvalidInfoUnstructured() {
        String errorMessage = getErrorMessage(ErrorMessages.INVALID_INFO_UNSTRUCTURED);

        Throwable thrown =
                catchThrowable(() -> HttpResponseExceptionHandler.checkForErrors(errorMessage));

        Assertions.assertThat(thrown).isInstanceOf(ReferenceValidationException.class);
        Assertions.assertThat(thrown).hasMessage(EndUserMessage.INVALID_MESSAGE.getKey().get());
    }

    @Test
    public void testIsRemittanceInfoSetForGirosPayment() {
        String errorMessage = getErrorMessage(ErrorMessages.REMITTANCE_INFO_NOT_SET_FOR_GIROS);

        Throwable thrown =
                catchThrowable(() -> HttpResponseExceptionHandler.checkForErrors(errorMessage));

        Assertions.assertThat(thrown).isInstanceOf(ReferenceValidationException.class);
        Assertions.assertThat(thrown).hasMessage(EndUserMessage.INVALID_MESSAGE.getKey().get());
    }

    @Test
    public void testIsServiceBlocked() {
        String errorMessage = getErrorMessage(ErrorMessages.SERVICE_BLOCKED);

        Throwable thrown =
                catchThrowable(() -> HttpResponseExceptionHandler.checkForErrors(errorMessage));

        Assertions.assertThat(thrown).isInstanceOf(PaymentRejectedException.class);
        Assertions.assertThat(thrown).hasMessage(PaymentRejectedException.MESSAGE);
    }

    @Test
    public void testIsInvalidCreditorAccount() {
        String errorMessage = getErrorMessage(ErrorMessages.INVALID_CREDITOR_ACCOUNT);

        Throwable thrown =
                catchThrowable(() -> HttpResponseExceptionHandler.checkForErrors(errorMessage));

        Assertions.assertThat(thrown).isInstanceOf(CreditorValidationException.class);
        Assertions.assertThat(thrown).hasMessage(EndUserMessage.INVALID_DESTINATION.getKey().get());
    }

    @Test
    public void testIsInvalidRequestedExecutionDate() {
        String errorMessage = getErrorMessage(ErrorMessages.INVALID_REQUESTED_EXECUTION_DATE);

        Throwable thrown =
                catchThrowable(() -> HttpResponseExceptionHandler.checkForErrors(errorMessage));

        Assertions.assertThat(thrown).isInstanceOf(DateValidationException.class);
        Assertions.assertThat(thrown).hasMessage(DateValidationException.DEFAULT_MESSAGE);
    }

    @Test
    public void testIsNotEnoughFundsToMakePayment() {
        String errorMessage = getErrorMessage(ErrorMessages.NOT_ENOUGH_FUNDS);

        Throwable thrown =
                catchThrowable(() -> HttpResponseExceptionHandler.checkForErrors(errorMessage));

        Assertions.assertThat(thrown).isInstanceOf(InsufficientFundsException.class);
        Assertions.assertThat(thrown).hasMessage(InsufficientFundsException.DEFAULT_MESSAGE);
    }

    private String getErrorMessage(String error) {
        return "Response statusCode: 400 with body: {\"tppMessages\":[{\"code\":\"FORMAT_ERROR\","
                + "\"text\":"
                + error
                + ",\"category\":\"ERROR\"}]}";
    }
}
