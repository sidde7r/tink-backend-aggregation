package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.executor.payment;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricConstants.Timer.WAITING_FOR_SUPPLEMENTAL_INFORMATION_MINUTES;

import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.executor.payment.enums.FabricPaymentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class FabricPaymentController {

    private final SupplementalInformationHelper supplementalInformationHelper;
    private final StrongAuthenticationState strongAuthenticationState;
    private long startPointPollingPendingStatus;
    private static final Logger logger = LoggerFactory.getLogger(FabricPaymentController.class);

    FabricPaymentController(
            SupplementalInformationHelper supplementalInformationHelper,
            StrongAuthenticationState strongAuthenticationState) {
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.strongAuthenticationState = strongAuthenticationState;
    }

    void redirectSCA(String redirectUrl) {
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

    private void waitForSupplementalInformation() {
        this.supplementalInformationHelper.waitForSupplementalInformation(
                strongAuthenticationState.getSupplementalKey(),
                WAITING_FOR_SUPPLEMENTAL_INFORMATION_MINUTES,
                TimeUnit.MINUTES);
        this.startPointPollingPendingStatus = System.currentTimeMillis();
    }

    PaymentMultiStepResponse response(
            CreatePaymentResponse createPaymentResponse,
            PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {

        FabricPaymentStatus paymentStatus =
                FabricPaymentStatus.fromString(createPaymentResponse.getTransactionStatus());
        switch (paymentStatus) {
                // After signing PIS
            case ACCP:
            case ACSC:
            case ACSP:
            case ACTC:
            case ACWC:
            case ACWP:
            case PDNG:
                return new PaymentMultiStepResponse(
                        createPaymentResponse.toTinkPaymentResponse(
                                paymentMultiStepRequest.getPayment()),
                        AuthenticationStepConstants.STEP_FINALIZE,
                        new ArrayList<>());
            case RCVD:
                return responsePendingPayment(paymentMultiStepRequest, createPaymentResponse);
            case RJCT:
            case CANC:
                throw new PaymentRejectedException();
            default:
                logger.error(
                        "Payment failed. Invalid Payment status from Fabric paymentStatus={}",
                        paymentStatus);
                throw new PaymentException("Payment failed");
        }
    }

    private PaymentMultiStepResponse responsePendingPayment(
            PaymentMultiStepRequest paymentMultiStepRequest,
            CreatePaymentResponse createPaymentResponse)
            throws PaymentAuthorizationException {
        if (System.currentTimeMillis() - this.startPointPollingPendingStatus
                >= FabricConstants.Timer.WAITING_FOR_QUIT_PENDING_STATUS_MILISEC) {
            logger.error(
                    "Payment timeout in {} status", createPaymentResponse.getTransactionStatus());
            throw new PaymentAuthorizationException();
        } else {
            return new PaymentMultiStepResponse(
                    createPaymentResponse.toTinkPaymentResponse(
                            paymentMultiStepRequest.getPayment()),
                    FabricConstants.PaymentStep.IN_PROGRESS,
                    new ArrayList<>());
        }
    }
}
