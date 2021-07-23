package se.tink.backend.aggregation.agents.utils.berlingroup.payment;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collections;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentCancelledException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.utils.berlingroup.common.LinksEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.PaymentConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.PaymentConstants.StorageValues;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.rpc.CreatePaymentResponse;
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

@AllArgsConstructor
@Slf4j
public class BasePaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {

    private static final String STEP_PENDING = "PENDING";

    private final PaymentApiClient apiClient;
    private final PaymentAuthenticator authenticator;
    private final SessionStorage sessionStorage;
    private PaymentStatusMapper paymentStatusMapper;

    public BasePaymentExecutor(
            PaymentApiClient apiClient,
            PaymentAuthenticator authenticator,
            SessionStorage sessionStorage) {
        this(apiClient, authenticator, sessionStorage, new BasePaymentStatusMapper());
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
        CreatePaymentResponse createPaymentResponse = apiClient.createPayment(paymentRequest);

        sessionStorage.put(StorageValues.SCA_LINKS, createPaymentResponse.getLinks());

        return createPaymentResponse.toTinkPayment(paymentRequest.getPayment());
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {
        if (AuthenticationStepConstants.STEP_INIT.equals(paymentMultiStepRequest.getStep())) {
            authorizePayment();
        }
        return checkStatus(paymentMultiStepRequest);
    }

    private void authorizePayment() {
        LinksEntity scaLinks =
                sessionStorage
                        .get(StorageValues.SCA_LINKS, LinksEntity.class)
                        .orElseThrow(
                                () -> new IllegalStateException(ErrorMessages.MISSING_SCA_URL));
        authenticator.authenticatePayment(scaLinks);
    }

    private PaymentMultiStepResponse checkStatus(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {
        Payment payment = paymentMultiStepRequest.getPayment();
        PaymentResponse paymentResponse = fetch(paymentMultiStepRequest);
        PaymentStatus paymentStatus = paymentResponse.getPayment().getStatus();
        log.info("Payment id={} sign status={}", payment.getId(), paymentStatus);

        switch (paymentStatus) {
            case SIGNED:
            case PAID:
                return new PaymentMultiStepResponse(
                        paymentResponse,
                        AuthenticationStepConstants.STEP_FINALIZE,
                        Collections.emptyList());
            case PENDING:
                return new PaymentMultiStepResponse(
                        paymentResponse, STEP_PENDING, Collections.emptyList());
            case REJECTED:
                throw new PaymentRejectedException("Payment rejected by Bank");
            case CANCELLED:
                throw new PaymentCancelledException("Payment Cancelled by PSU");
            default:
                log.error("Payment in unexpected status after signing: {}", paymentStatus);
                throw new PaymentAuthorizationException();
        }
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) {
        return apiClient
                .fetchPaymentStatus(paymentRequest)
                .toTinkPayment(paymentRequest.getPayment(), paymentStatusMapper);
    }

    @Override
    public CreateBeneficiaryMultiStepResponse createBeneficiary(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest) {
        throw new NotImplementedException(
                "createBeneficiary not yet implemented for " + this.getClass().getName());
    }

    @Override
    public PaymentResponse cancel(PaymentRequest paymentRequest) {
        throw new NotImplementedException(
                "cancel not yet implemented for " + this.getClass().getName());
    }

    @Override
    public PaymentListResponse fetchMultiple(PaymentListRequest paymentListRequest) {
        throw new NotImplementedException(
                "fetchMultiple not yet implemented for " + this.getClass().getName());
    }

    public static LocalDate createStartDateForRecurringPayment(int dayShift) {
        LocalDate startDate = LocalDate.now().plusDays(dayShift);
        int shift = 0;
        if (startDate.getDayOfWeek() == DayOfWeek.SATURDAY) {
            shift = 2;
        } else if (startDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
            shift = 1;
        }
        return startDate.plusDays(shift);
    }
}
