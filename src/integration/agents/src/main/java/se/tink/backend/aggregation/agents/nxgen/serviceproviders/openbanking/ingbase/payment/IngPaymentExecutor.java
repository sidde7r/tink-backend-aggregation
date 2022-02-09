package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentCancelledException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentValidationException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment.rpc.IngCreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment.rpc.IngCreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment.rpc.IngPaymentStatusResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.FetchablePaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentListRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentListResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payments.common.model.PaymentScheme;
import se.tink.libraries.transfer.rpc.PaymentServiceType;

@Slf4j
@RequiredArgsConstructor
public class IngPaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {

    private final SessionStorage sessionStorage;
    private final IngPaymentApiClient paymentApiClient;
    private final IngPaymentAuthenticator paymentAuthenticator;
    private final IngPaymentMapper paymentMapper;
    private final boolean instantSepaIsSupported;

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentRejectedException {
        Payment payment = paymentRequest.getPayment();

        IngCreatePaymentRequest createPaymentRequest = createPaymentRequest(payment);
        IngCreatePaymentResponse createPaymentResponse =
                paymentApiClient.createPayment(
                        createPaymentRequest, payment.getPaymentServiceType());

        savePaymentAuthorizationUrl(createPaymentResponse);
        updateCreatedPayment(payment, createPaymentResponse);
        return new PaymentResponse(payment);
    }

    private IngCreatePaymentRequest createPaymentRequest(Payment payment)
            throws PaymentRejectedException {
        // Temporary solution to be fixed in NZG-1112
        if (PaymentScheme.SEPA_INSTANT_CREDIT_TRANSFER == payment.getPaymentScheme()
                && !instantSepaIsSupported) {
            throw new PaymentValidationException("Instant payment is not supported");
        }
        if (PaymentServiceType.PERIODIC.equals(payment.getPaymentServiceType())) {
            return paymentMapper.toIngCreateRecurringPaymentRequest(payment);
        }
        return paymentMapper.toIngCreatePaymentRequest(payment);
    }

    private void savePaymentAuthorizationUrl(IngCreatePaymentResponse createPaymentResponse) {
        String authorizationUrl = createPaymentResponse.getLinks().getScaRedirect();
        sessionStorage.put(
                StorageKeys.PAYMENT_AUTHORIZATION_URL,
                IngPaymentUtils.modifyMarketCode(
                        authorizationUrl, paymentApiClient.getMarketCode()));
    }

    private String readPaymentAuthorizationUrl() {
        return Optional.ofNullable(sessionStorage.get(StorageKeys.PAYMENT_AUTHORIZATION_URL))
                .orElseThrow(
                        () -> new IllegalStateException("[ING] Missing authorize payment url"));
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {
        String authorizationUrl = readPaymentAuthorizationUrl();
        boolean callbackReceived = paymentAuthenticator.waitForUserConfirmation(authorizationUrl);

        PaymentResponse paymentResponse = fetch(paymentMultiStepRequest);
        PaymentStatus paymentStatus = paymentResponse.getPayment().getStatus();
        switch (paymentStatus) {
            case SIGNED:
            case PAID:
                return new PaymentMultiStepResponse(
                        paymentResponse, AuthenticationStepConstants.STEP_FINALIZE);
            case REJECTED:
                throw new PaymentRejectedException("[ING] Payment rejected by Bank");
            case CANCELLED:
                throw new PaymentCancelledException("[ING] Payment cancelled by PSU");
            case PENDING:
                /*
                If user does not provide credentials, transaction ends up with status RCVD and can't be cancelled.
                If user provides credentials but aborts transaction later it ends up with different status
                and must be cancelled otherwise it is possible to approve it later.
                */
                cancel(paymentMultiStepRequest);
                return handleTransationAbortedCase(callbackReceived);
            case USER_APPROVAL_FAILED:
                return handleTransationAbortedCase(callbackReceived);
            default:
                throw new PaymentAuthorizationException(
                        "[ING] Payment was not signed even after SCA, status: " + paymentStatus);
        }
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) {
        Payment payment = paymentRequest.getPayment();

        IngPaymentStatusResponse paymentStatusResponse =
                paymentApiClient.getPaymentStatus(
                        payment.getUniqueId(), payment.getPaymentServiceType());

        log.info(
                "[ING] Fetched transaction status: {}",
                paymentStatusResponse.getTransactionStatus());
        updatePaymentStatus(payment, paymentStatusResponse.getTransactionStatus());
        return new PaymentResponse(payment);
    }

    private void updateCreatedPayment(
            Payment payment, IngCreatePaymentResponse createPaymentResponse) {
        updatePaymentStatus(payment, createPaymentResponse.getTransactionStatus());
        payment.setUniqueId(createPaymentResponse.getPaymentId());
    }

    private void updatePaymentStatus(Payment payment, String transactionStatus) {
        PaymentStatus paymentStatus = paymentMapper.getPaymentStatus(transactionStatus);
        payment.setStatus(paymentStatus);
    }

    @Override
    public CreateBeneficiaryMultiStepResponse createBeneficiary(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest) {
        throw new NotImplementedException(
                "createBeneficiary not yet implemented for " + this.getClass().getName());
    }

    @Override
    public PaymentResponse cancel(PaymentRequest paymentRequest) {
        Payment payment = paymentRequest.getPayment();
        paymentApiClient.cancelPayment(payment.getUniqueId(), payment.getPaymentServiceType());

        payment.setStatus(PaymentStatus.CANCELLED);
        return PaymentResponse.of(paymentRequest);
    }

    @Override
    public PaymentListResponse fetchMultiple(PaymentListRequest paymentListRequest) {
        throw new NotImplementedException(
                "fetchMultiple not yet implemented for " + this.getClass().getName());
    }

    private PaymentMultiStepResponse handleTransationAbortedCase(boolean callbackReceived)
            throws PaymentCancelledException {
        if (callbackReceived) {
            throw new PaymentCancelledException(
                    "[ING] User left authorization page without approving request");

        } else {
            /*
            Current ING flow seems to be broken and when user cancels payment in their app we don't receive any
            callback and the payment's status does not change.
             */
            throw new PaymentCancelledException(
                    "[ING] No callback received - payment cancelled or ignored");
        }
    }
}
