package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient.error;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.payment.CreditorValidationException;
import se.tink.backend.aggregation.agents.exceptions.payment.DebtorValidationException;
import se.tink.backend.aggregation.agents.exceptions.payment.InsufficientFundsException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient.dto.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class DemobankErrorHandlerTest {

    private final DemobankErrorHandler handler = new DemobankErrorHandler();

    @Test(expected = PaymentException.class)
    public void throws_payment_exception_on_unknown_exception() throws PaymentException {
        ErrorResponse errorResponse = mock(ErrorResponse.class);
        HttpResponseException exception = mockException(errorResponse);

        handler.remapException(exception);
    }

    @Test(expected = InsufficientFundsException.class)
    public void throws_insufficient_funds_exception_on_unknown_exception() throws PaymentException {
        // given
        ErrorResponse errorResponse = mock(ErrorResponse.class);
        when(errorResponse.hasInsufficientFunds()).thenReturn(true);

        HttpResponseException exception = mockException(errorResponse);

        // when
        handler.remapException(exception);
    }

    @Test(expected = DebtorValidationException.class)
    public void throws_debtor_validation_exception_on_unknown_exception() throws PaymentException {
        // given
        ErrorResponse errorResponse = mock(ErrorResponse.class);
        when(errorResponse.isInvalidDebtor()).thenReturn(true);

        HttpResponseException exception = mockException(errorResponse);

        // when
        handler.remapException(exception);
    }

    @Test(expected = CreditorValidationException.class)
    public void throws_creditor_validation_exception_on_unknown_exception()
            throws PaymentException {
        // given
        ErrorResponse errorResponse = mock(ErrorResponse.class);
        when(errorResponse.isInvalidCreditor()).thenReturn(true);

        HttpResponseException exception = mockException(errorResponse);

        // when
        handler.remapException(exception);
    }

    @Test(expected = PaymentRejectedException.class)
    public void throws_rejected_exception_on_unknown_exception() throws PaymentException {
        // given
        ErrorResponse errorResponse = mock(ErrorResponse.class);
        when(errorResponse.isBadRequest()).thenReturn(true);

        HttpResponseException exception = mockException(errorResponse);

        // when
        handler.remapException(exception);
    }

    private HttpResponseException mockException(ErrorResponse errorResponse) {
        HttpResponseException exception = mock(HttpResponseException.class, RETURNS_DEEP_STUBS);
        when(exception.getResponse().getBody(ErrorResponse.class)).thenReturn(errorResponse);
        return exception;
    }
}
