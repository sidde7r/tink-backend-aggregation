package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common;

import se.tink.backend.aggregation.agents.exceptions.payment.CreditorValidationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentValidationException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.rpc.UkObErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentConstants.ErrorCode;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentConstants.ErrorMessage;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.signableoperation.enums.InternalStatus;

public class UkOpenBankingPaymentErrorHandler {

    public static PaymentException getPaymentError(HttpResponseException e) {
        HttpResponse httpResponse = e.getResponse();
        UkObErrorResponse body = httpResponse.getBody(UkObErrorResponse.class);
        if (body != null) {

            if (body.getErrorMessages().contains(ErrorMessage.EXCEED_DAILY_LIMIT_FAILURE)) {
                return new PaymentRejectedException(
                        ErrorMessage.EXCEED_DAILY_LIMIT_FAILURE,
                        InternalStatus.TRANSFER_LIMIT_REACHED);
            } else if (body.getErrorMessages()
                    .contains(ErrorMessage.PAYMENT_RE_AUTHENTICATION_REQUIRED)) {
                return new PaymentAuthorizationException(
                        "Your payment request could not be authorized by the bank at the moment. Please try executing payment again.",
                        InternalStatus.PAYMENT_AUTHORIZATION_FAILED);
            } else if (body.isErrorCode(ErrorMessage.PROFILE_IS_RESTRICTED)) {
                return new PaymentRejectedException(
                        "Profile is restricted.", InternalStatus.ACCOUNT_BLOCKED_FOR_TRANSFER);
            } else if (body.getErrorMessages().contains(ErrorMessage.SUSPICIOUS_TRANSACTION)) {
                return new PaymentRejectedException(
                        "Bank systems have identified your transaction as highly suspicious.",
                        InternalStatus.ACCOUNT_BLOCKED_FOR_TRANSFER);
            } else if (body.getErrorMessages().contains(ErrorMessage.SAME_SENDER_AND_RECIPIENT)) {
                return new PaymentValidationException(
                        "Sender and recipient can not be the same.",
                        InternalStatus.INVALID_DESTINATION_ACCOUNT);
            } else if (body.isErrorCode(ErrorCode.BARCLAYS_DAILY_LIMIT_REACHED)
                    || body.hasErrorCode(ErrorCode.BARCLAYS_DAILY_LIMIT_REACHED)) {
                return new PaymentRejectedException(
                        "Daily limit reached.", InternalStatus.TRANSFER_LIMIT_REACHED);
            } else if (body.getErrorMessages().stream()
                    .anyMatch(
                            message ->
                                    message.contains(
                                            ErrorMessage.PAYMENTS_IN_EUR_ARE_NOT_AVAILABLE))) {
                return new CreditorValidationException(
                        "Domestic payments in EUR are not available.",
                        InternalStatus.INVALID_DESTINATION_ACCOUNT);
            }
        }
        return new PaymentException(InternalStatus.BANK_ERROR_CODE_NOT_HANDLED_YET);
    }
}
