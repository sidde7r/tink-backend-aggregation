package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment;

import java.util.Collections;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentCancelledException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
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
import se.tink.libraries.transfer.rpc.PaymentServiceType;

@Slf4j
@RequiredArgsConstructor
public class IngPaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {

    private final SessionStorage sessionStorage;
    private final IngPaymentApiClient paymentApiClient;
    private final IngPaymentAuthenticator paymentAuthenticator;
    private final IngPaymentMapper paymentMapper;

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) {
        Payment payment = paymentRequest.getPayment();

        IngCreatePaymentRequest createPaymentRequest = createPaymentRequest(payment);
        IngCreatePaymentResponse createPaymentResponse =
                paymentApiClient.createPayment(
                        createPaymentRequest, payment.getPaymentServiceType());

        savePaymentAuthorizationUrl(createPaymentResponse);
        updateCreatedPayment(payment, createPaymentResponse);
        return new PaymentResponse(payment);
    }

    private IngCreatePaymentRequest createPaymentRequest(Payment payment) {
        if (PaymentServiceType.PERIODIC.equals(payment.getPaymentServiceType())) {
            return paymentMapper.toIngCreateRecurringPaymentRequest(payment);
        }
        return paymentMapper.toIngCreatePaymentRequest(payment);
    }

    private void savePaymentAuthorizationUrl(IngCreatePaymentResponse createPaymentResponse) {
        String authorizationUrl = createPaymentResponse.getLinks().getScaRedirect();
        sessionStorage.put(StorageKeys.PAYMENT_AUTHORIZATION_URL, authorizationUrl);
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
        paymentAuthenticator.authenticate(authorizationUrl);

        PaymentResponse paymentResponse = fetch(paymentMultiStepRequest);
        PaymentStatus paymentStatus = paymentResponse.getPayment().getStatus();
        switch (paymentStatus) {
            case SIGNED:
            case PAID:
                return new PaymentMultiStepResponse(
                        paymentResponse,
                        AuthenticationStepConstants.STEP_FINALIZE,
                        Collections.emptyList());
            case REJECTED:
                throw new PaymentRejectedException("[ING] Payment rejected by Bank");
            case CANCELLED:
                throw new PaymentCancelledException("[ING] Payment cancelled by PSU");
            case PENDING:
                /*
                On ING page user has an option to either:
                 - click nothing and simply accept the request in the app
                 - click "Back" or "Cancel"
                Clicking any of those button does not actually cancel the request and it can still be
                approved later - even after few hours. Because we don't want to leave agent hanging for that long,
                we should treat this as if user has cancelled the request.
                 */
                cancel(paymentMultiStepRequest);
                throw new PaymentCancelledException(
                        "[ING] User left authorization page without approving request");
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
}
