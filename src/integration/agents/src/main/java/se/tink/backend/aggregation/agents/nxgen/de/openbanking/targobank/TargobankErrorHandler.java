package se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.BankverlagErrorHandler;
import se.tink.backend.aggregation.agents.utils.berlingroup.error.ErrorResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.error.TppMessage;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class TargobankErrorHandler extends BankverlagErrorHandler {

    private static final TppMessage.TppMessageBuilder PAYMENT_FAILED =
            TppMessage.builder().category(TppMessage.ERROR).code("PAYMENT_FAILED");

    private static final TppMessage PSU_CREDENTIALS_INVALID =
            TppMessage.builder()
                    .category(TppMessage.ERROR)
                    .code("PSU_CREDENTIALS_INVALID")
                    .text("9050:***Die eingegebenen Daten sind falsch.***")
                    .build();

    private static final TppMessage FIRST_PAYMENT_ONLINE_BANKING =
            PAYMENT_FAILED
                    .text(
                            "3999:***Sie müssen die erste Echtzeitüberweisung im Online-Banking ausführen.***")
                    .build();

    private static final TppMessage PAYMENT_FAILED_CONTACT_WITH_BANK =
            PAYMENT_FAILED
                    .text(
                            "3999:***Wir haben Rückfragen zum Auftrag.\u00A0Bitte rufen Sie uns an: 0211-56156096.***")
                    .build();

    private static final TppMessage PAYMENT_FAILED_RECIPIENT_BANK_NOT_SUPPORTING_INSTANT_PAYMENTS =
            PAYMENT_FAILED
                    .text(
                            "9210:Die Empfängerbank bietet den Empfang von Echtzeitüberweisungen aktuell nicht an.")
                    .build();

    private static final Map<TppMessage, String> paymentRejectionMapping =
            ImmutableMap.of(
                    FIRST_PAYMENT_ONLINE_BANKING,
                    "Payment rejected, you must make the first instant transfer in online banking.",
                    PAYMENT_FAILED_CONTACT_WITH_BANK,
                    "Payment rejected, please contact with bank.");

    @Override
    protected Optional<RuntimeException> findErrorForResponse(
            ErrorResponse errorResponse,
            ErrorSource errorSource,
            HttpResponseException httpResponseException) {
        Optional<RuntimeException> runtimeException =
                super.findErrorForResponse(errorResponse, errorSource, httpResponseException);
        if (runtimeException.isPresent()) {
            return runtimeException;
        } else {
            if (errorSource == ErrorSource.SELECT_AUTHORIZATION_METHOD) {
                return handleSelectAuthorizationMethod(errorResponse, httpResponseException);
            }
            if (errorSource == ErrorSource.GET_AUTHORIZATION_STATUS) {
                return handleGetAuthorizationStatus(errorResponse, httpResponseException);
            }
            return Optional.empty();
        }
    }

    @Override
    protected Optional<RuntimeException> handleUsernamePasswordErrors(
            ErrorResponse errorResponse, HttpResponseException httpResponseException) {
        if (ErrorResponse.anyTppMessageMatchesPredicate(PSU_CREDENTIALS_INVALID)
                .test(errorResponse)) {
            return Optional.of(LoginError.INCORRECT_CREDENTIALS.exception(httpResponseException));
        }
        return handlePaymentRejections(errorResponse, httpResponseException);
    }

    private Optional<RuntimeException> handleSelectAuthorizationMethod(
            ErrorResponse errorResponse, HttpResponseException httpResponseException) {
        return handlePaymentRejections(errorResponse, httpResponseException);
    }

    private Optional<RuntimeException> handleGetAuthorizationStatus(
            ErrorResponse errorResponse, HttpResponseException httpResponseException) {
        if (ErrorResponse.anyTppMessageMatchesPredicate(
                        PAYMENT_FAILED_RECIPIENT_BANK_NOT_SUPPORTING_INSTANT_PAYMENTS)
                .test(errorResponse)) {
            return Optional.of(
                    new PaymentRejectedException(
                            "Payment rejected, recipient bank doesn't support instant payments.",
                            httpResponseException));
        }
        return Optional.empty();
    }

    private Optional<RuntimeException> handlePaymentRejections(
            ErrorResponse errorResponse, HttpResponseException httpResponseException) {
        for (TppMessage tppMessage : paymentRejectionMapping.keySet()) {
            if (ErrorResponse.anyTppMessageMatchesPredicate(tppMessage).test(errorResponse)) {
                return Optional.of(
                        new PaymentRejectedException(
                                paymentRejectionMapping.get(tppMessage), httpResponseException));
            }
        }
        return Optional.empty();
    }
}
