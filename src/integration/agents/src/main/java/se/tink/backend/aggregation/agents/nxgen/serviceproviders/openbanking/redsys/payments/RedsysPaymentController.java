package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.payments;

import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.PaymentStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.Timer;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.payments.src.PaymentStatusResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.signableoperation.enums.InternalStatus;

@Slf4j
public class RedsysPaymentController {

    private final SupplementalInformationHelper supplementalInformationHelper;
    private final StrongAuthenticationState strongAuthenticationState;
    private long startPointPollingPendingStatus;

    RedsysPaymentController(
            SupplementalInformationHelper supplementalInformationHelper,
            StrongAuthenticationState strongAuthenticationState) {
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.strongAuthenticationState = strongAuthenticationState;
    }

    void redirectSCA(String redirectUrl) throws PaymentAuthorizationException {
        openThirdPartyApp(redirectUrl);
        waitForSupplementalInformation();
    }

    private void openThirdPartyApp(String redirectUrl) {
        ThirdPartyAppAuthenticationPayload payload = getAppPayload(new URL(redirectUrl));
        Preconditions.checkNotNull(payload);
        this.supplementalInformationHelper.openThirdPartyApp(payload);
    }

    private ThirdPartyAppAuthenticationPayload getAppPayload(URL authorizeUrl) {
        return ThirdPartyAppAuthenticationPayload.of(authorizeUrl);
    }

    private void waitForSupplementalInformation() throws PaymentAuthorizationException {
        this.supplementalInformationHelper
                .waitForSupplementalInformation(
                        strongAuthenticationState.getSupplementalKey(), 5, TimeUnit.MINUTES)
                .orElseThrow(
                        () ->
                                new PaymentAuthorizationException(
                                        "SCA time-out.",
                                        InternalStatus.PAYMENT_AUTHORIZATION_TIMEOUT,
                                        ThirdPartyAppError.TIMED_OUT.exception()));

        this.startPointPollingPendingStatus = System.currentTimeMillis();
    }

    PaymentMultiStepResponse response(
            PaymentStatusResponse paymentStatusResponse,
            PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {

        PaymentStatus tinkPaymentStatus =
                paymentStatusResponse.getTransactionStatus().getTinkStatus();

        switch (tinkPaymentStatus) {
            case PAID:
                return new PaymentMultiStepResponse(
                        paymentStatusResponse.toTinkPayment(paymentMultiStepRequest.getPayment()),
                        AuthenticationStepConstants.STEP_FINALIZE);
            case PENDING:
                return responsePendingPayment(paymentMultiStepRequest, paymentStatusResponse);
            case REJECTED:
            case CANCELLED:
                throw new PaymentRejectedException();
            default:
                log.error(
                        "Payment failed. Invalid Payment status from Redsys paymentStatus={}",
                        paymentStatusResponse.getTransactionStatus());
                throw new PaymentException(InternalStatus.BANK_ERROR_CODE_NOT_HANDLED_YET);
        }
    }

    private PaymentMultiStepResponse responsePendingPayment(
            PaymentMultiStepRequest paymentMultiStepRequest,
            PaymentStatusResponse paymentStatusResponse)
            throws PaymentAuthorizationException {
        log.info("Payment in {} status", paymentStatusResponse.getTransactionStatus());
        if (System.currentTimeMillis() - this.startPointPollingPendingStatus
                >= Timer.WAITING_FOR_QUIT_PENDING_STATUS_MILISEC) {
            log.error("Payment timeout in {} status", paymentStatusResponse.getTransactionStatus());
            throw new PaymentAuthorizationException(InternalStatus.PAYMENT_AUTHORIZATION_TIMEOUT);
        } else {
            return new PaymentMultiStepResponse(
                    paymentStatusResponse.toTinkPayment(paymentMultiStepRequest.getPayment()),
                    PaymentStep.IN_PROGRESS);
        }
    }
}
