package se.tink.backend.aggregation.agents.utils.berlingroup.payment;

import java.time.DayOfWeek;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentCancelledException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.utils.berlingroup.common.LinksEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.PaymentConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.PaymentConstants.StorageValues;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.enums.AspspPaymentStatus;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.rpc.FetchPaymentStatusResponse;
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
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Payment;

@AllArgsConstructor
@Slf4j
public class BasePaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {

    private static final String STEP_PENDING = "PENDING";

    private final PaymentApiClient apiClient;
    private final PaymentAuthenticator authenticator;
    private final SessionStorage sessionStorage;
    private final PaymentStatusMapper paymentStatusMapper;

    public BasePaymentExecutor(
            PaymentApiClient apiClient,
            PaymentAuthenticator authenticator,
            SessionStorage sessionStorage) {
        this(apiClient, authenticator, sessionStorage, new BasePaymentStatusMapper());
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) {
        CreatePaymentResponse createPaymentResponse = apiClient.createPayment(paymentRequest);

        sessionStorage.put(StorageValues.SCA_LINKS, createPaymentResponse.getLinks());

        return createPaymentResponse.toTinkPayment(paymentRequest.getPayment());
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest) {
        if (AuthenticationStepConstants.STEP_INIT.equals(paymentMultiStepRequest.getStep())) {
            try {
                authorizePayment();
            } catch (AgentException exception) {
                debugLoggingForUnexpectedFailures(exception, paymentMultiStepRequest.getPayment());
                throw exception;
            }
        }

        PaymentMultiStepResponse paymentMultiStepResponse = checkStatus(paymentMultiStepRequest);
        debugLoggingForSucessfulPayments(paymentMultiStepResponse);

        return paymentMultiStepResponse;
    }

    protected void debugLoggingForUnexpectedFailures(
            AgentException agentException, Payment payment) {
        // Place for logging for all agents reusing this class, if ever necessary.
        // Per agent logging should live in extensions of this class.
    }

    private void debugLoggingForSucessfulPayments(
            PaymentMultiStepResponse paymentMultiStepResponse) {
        // NZG-1028 Temporary logging to learn more.
        // We want to observe successes that go beyond borders.
        if (AuthenticationStepConstants.STEP_FINALIZE.equalsIgnoreCase(
                paymentMultiStepResponse.getStep())) {

            try {
                String countryCodeFromIban =
                        paymentMultiStepResponse
                                .getPayment()
                                .getCreditor()
                                .getAccountIdentifier(IbanIdentifier.class)
                                .getIban()
                                .substring(0, 2);
                if (!"DE".equalsIgnoreCase(countryCodeFromIban)) {
                    log.info("Payment succeeded after crossing an iban border!");
                }
            } catch (RuntimeException exception) {
                log.warn(
                        "BasePaymentExecutor caught exception while trying to do some logging! This should not happen.",
                        exception);
            }
        }
    }

    private void authorizePayment() {
        LinksEntity scaLinks =
                sessionStorage
                        .get(StorageValues.SCA_LINKS, LinksEntity.class)
                        .orElseThrow(
                                () -> new IllegalStateException(ErrorMessages.MISSING_SCA_URL));
        authenticator.authenticatePayment(scaLinks);
    }

    private PaymentMultiStepResponse checkStatus(PaymentMultiStepRequest paymentMultiStepRequest) {
        Payment payment = paymentMultiStepRequest.getPayment();
        PaymentResponse paymentResponse = fetch(paymentMultiStepRequest);
        PaymentStatus paymentStatus = paymentResponse.getPayment().getStatus();
        log.info("Payment id={} sign status={}", payment.getId(), paymentStatus);

        switch (paymentStatus) {
            case SIGNED:
            case PAID:
                return new PaymentMultiStepResponse(
                        paymentResponse, AuthenticationStepConstants.STEP_FINALIZE);
            case PENDING:
                return new PaymentMultiStepResponse(paymentResponse, STEP_PENDING);
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
        FetchPaymentStatusResponse fetchPaymentStatusResponse =
                apiClient.fetchPaymentStatus(paymentRequest);
        logDetailsForRejectedPayment(fetchPaymentStatusResponse);
        return fetchPaymentStatusResponse.toTinkPayment(
                paymentRequest.getPayment(), paymentStatusMapper);
    }

    private void logDetailsForRejectedPayment(
            FetchPaymentStatusResponse fetchPaymentStatusResponse) {
        AspspPaymentStatus paymentStatus =
                AspspPaymentStatus.fromString(fetchPaymentStatusResponse.getTransactionStatus());
        if (paymentStatus == AspspPaymentStatus.REJECTED) {
            log.info(
                    "Payment rejected psuMessage: {} fundsAvailable: {}",
                    fetchPaymentStatusResponse.getPsuMessage(),
                    fetchPaymentStatusResponse.getFundsAvailable());
        }
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

    public static LocalDate createStartDateForRecurringPaymentWithWorkdays(int dayShift) {
        LocalDate startDate = LocalDate.now();
        while (dayShift > 0) {
            startDate = startDate.plusDays(1);
            if (isNotWeekend(startDate)) {
                dayShift--;
            }
        }
        return startDate;
    }

    private static boolean isNotWeekend(LocalDate startDate) {
        return startDate.getDayOfWeek() != DayOfWeek.SATURDAY
                && startDate.getDayOfWeek() != DayOfWeek.SUNDAY;
    }
}
