package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment;

import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentValidationException;
import se.tink.backend.aggregation.agents.utils.berlingroup.error.ErrorResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.error.TppMessage;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class FinecoPaymentErrorHandler {

    private static final TppMessage REQ_DATE_INVALID =
            TppMessage.builder()
                    .category("ERROR")
                    .code("FORMAT_ERROR")
                    .text("The RequestedExecutionDate is not valid. First available date is ")
                    .build();

    public static void checkForErrors(HttpResponseException exception) throws PaymentException {
        Optional<ErrorResponse> errorResponse = ErrorResponse.fromHttpException(exception);
        if (errorResponse
                .filter(
                        ErrorResponse.anyTppMessageMatchesPredicate(
                                REQ_DATE_INVALID, String::contains))
                .isPresent()) {
            throw new PaymentValidationException(
                    PaymentValidationException.DEFAULT_MESSAGE, exception);
        }
    }
}
