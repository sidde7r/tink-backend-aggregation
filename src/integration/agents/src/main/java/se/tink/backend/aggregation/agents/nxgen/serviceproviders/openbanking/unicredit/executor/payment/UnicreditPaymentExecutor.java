package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment;

import static se.tink.libraries.payment.enums.PaymentStatus.CANCELLED;
import static se.tink.libraries.payment.enums.PaymentStatus.PAID;
import static se.tink.libraries.payment.enums.PaymentStatus.REJECTED;
import static se.tink.libraries.payment.enums.PaymentStatus.SIGNED;
import static se.tink.libraries.transfer.enums.RemittanceInformationType.UNSTRUCTURED;

import com.github.rholder.retry.RetryException;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentCancelledException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditApiClientRetryer;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment.entity.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment.entity.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment.entity.RemittanceInformationStructuredEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment.rpc.CreateRecurringPaymentRequest;
import se.tink.backend.aggregation.agents.utils.remittanceinformation.RemittanceInformationValidator;
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
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.PaymentServiceType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

@Slf4j
@AllArgsConstructor
public class UnicreditPaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {

    private final UnicreditBaseApiClient apiClient;
    private final UnicreditApiClientRetryer unicreditApiClientRetryer;

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) {

        PaymentType type =
                UnicreditConstants.PAYMENT_TYPE_MAPPER
                        .translate(paymentRequest.getPayment().getCreditorAndDebtorAccountType())
                        .orElse(PaymentType.UNDEFINED);

        CreatePaymentRequest createPaymentRequest;

        if (PaymentServiceType.PERIODIC.equals(
                paymentRequest.getPayment().getPaymentServiceType())) {
            createPaymentRequest = getCreateRecurringPaymentRequest(paymentRequest.getPayment());
        } else {
            createPaymentRequest = getCreatePaymentRequest(paymentRequest.getPayment());
        }

        return apiClient
                .createSepaPayment(createPaymentRequest, paymentRequest)
                .toTinkPayment(
                        paymentRequest.getPayment().getDebtor().getAccountNumber(),
                        paymentRequest.getPayment().getCreditor().getAccountNumber(),
                        type);
    }

    private CreatePaymentRequest getCreatePaymentRequest(Payment payment) {

        CreatePaymentRequest.CreatePaymentRequestBuilder<?, ?> requestBuilder =
                CreatePaymentRequest.builder()
                        .creditorAccount(getAccountEntity(payment.getCreditor().getAccountNumber()))
                        .debtorAccount(getAccountEntity(payment.getDebtor().getAccountNumber()))
                        .instructedAmount(getAmountEntity(payment))
                        .creditorName(payment.getCreditor().getName())
                        .requestedExecutionDate(
                                payment.getExecutionDate() != null
                                        ? payment.getExecutionDate().toString()
                                        : null);

        addRemittanceInformation(requestBuilder, payment);

        return requestBuilder.build();
    }

    private CreatePaymentRequest getCreateRecurringPaymentRequest(Payment payment) {

        CreateRecurringPaymentRequest.CreateRecurringPaymentRequestBuilder<?, ?> requestBuilder =
                CreateRecurringPaymentRequest.builder()
                        .creditorAccount(getAccountEntity(payment.getCreditor().getAccountNumber()))
                        .debtorAccount(getAccountEntity(payment.getDebtor().getAccountNumber()))
                        .instructedAmount(getAmountEntity(payment))
                        .creditorName(payment.getCreditor().getName())
                        .frequency(payment.getFrequency().toString())
                        .startDate(payment.getStartDate().toString())
                        // optional attributes
                        .endDate(
                                payment.getEndDate() != null
                                        ? payment.getEndDate().toString()
                                        : null)
                        .executionRule(
                                payment.getExecutionRule() != null
                                        ? payment.getExecutionRule().toString()
                                        : null)
                        .dayOfExecution(getDayOfExecution(payment));

        addRemittanceInformation(requestBuilder, payment);

        return requestBuilder.build();
    }

    private AmountEntity getAmountEntity(Payment payment) {
        return new AmountEntity(
                String.valueOf(payment.getExactCurrencyAmount().getDoubleValue()),
                payment.getExactCurrencyAmount().getCurrencyCode());
    }

    private void addRemittanceInformation(
            CreatePaymentRequest.CreatePaymentRequestBuilder<?, ?> requestBuilder,
            Payment payment) {
        RemittanceInformationType remittanceInformationType =
                payment.getRemittanceInformation().getType();

        switch (remittanceInformationType) {
            case UNSTRUCTURED:
                requestBuilder.remittanceInformationUnstructured(
                        getUnstructuredRemittance(payment));
                break;
            case OCR:
            case REFERENCE:
                requestBuilder.remittanceInformationStructured(
                        new RemittanceInformationStructuredEntity(
                                payment.getRemittanceInformation().getValue(),
                                remittanceInformationType.name()));
                break;
            default:
                throw new IllegalArgumentException(
                        "Unsupported remittance information type: "
                                + remittanceInformationType.name());
        }
    }

    private String getUnstructuredRemittance(Payment payment) {
        RemittanceInformation remittanceInformation = payment.getRemittanceInformation();

        RemittanceInformationValidator.validateSupportedRemittanceInformationTypesOrThrow(
                remittanceInformation, null, UNSTRUCTURED);

        return Optional.ofNullable(remittanceInformation.getValue()).orElse("");
    }

    private AccountEntity getAccountEntity(String accountNumber) {
        return new AccountEntity(accountNumber);
    }

    private String getDayOfExecution(Payment payment) {
        switch (payment.getFrequency()) {
            case WEEKLY:
                return String.valueOf(payment.getDayOfWeek().getValue());
            case MONTHLY:
                return payment.getDayOfMonth().toString();
            default:
                throw new IllegalArgumentException(
                        "Frequency is not supported: " + payment.getFrequency());
        }
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {
        PaymentResponse paymentResponse = getPaymentStatus(paymentMultiStepRequest);
        PaymentStatus paymentStatus = paymentResponse.getPayment().getStatus();
        log.info(
                "Payment id={} sign status={}",
                paymentMultiStepRequest.getPayment().getId(),
                paymentStatus);
        if (SIGNED.equals(paymentStatus) || PAID.equals(paymentStatus)) {
            return new PaymentMultiStepResponse(
                    paymentResponse, AuthenticationStepConstants.STEP_FINALIZE);
        } else if (REJECTED.equals(paymentStatus)) {
            throw new PaymentRejectedException("Payment rejected by Bank");
        } else if (CANCELLED.equals(paymentStatus)) {
            throw new PaymentCancelledException("Payment Cancelled by PSU");
        } else {

            // payment status= RCVD
            log.error("Payment was not signed even after waiting for 9 min");
            throw new PaymentAuthorizationException();
        }
    }

    private PaymentResponse getPaymentStatus(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {
        PaymentResponse paymentResponse;
        try {
            paymentResponse =
                    unicreditApiClientRetryer.callUntilPaymentStatusIsNotPending(
                            () -> fetchPaymentWithId(paymentMultiStepRequest));
        } catch (RetryException e) {
            return assumePaymentWasInitializedCorrectly(paymentMultiStepRequest);
        } catch (ExecutionException e) {
            throw new PaymentException(e.getMessage(), e.getCause());
        }
        return paymentResponse;
    }

    private PaymentResponse assumePaymentWasInitializedCorrectly(
            PaymentMultiStepRequest paymentMultiStepRequest) {
        Payment payment = paymentMultiStepRequest.getPayment();
        payment.setStatus(SIGNED);
        return new PaymentResponse(payment);
    }

    private PaymentResponse fetchPaymentWithId(PaymentRequest paymentRequest) {
        return apiClient
                .fetchPaymentStatus(paymentRequest)
                .toTinkPayment(paymentRequest.getPayment());
    }

    @Override
    public CreateBeneficiaryMultiStepResponse createBeneficiary(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest) {
        throw new NotImplementedException(
                "Create beneficiary not implemented for " + this.getClass().getName());
    }

    @Override
    public PaymentResponse cancel(PaymentRequest paymentRequest) {
        throw new NotImplementedException(
                "Cancel not implemented for " + this.getClass().getName());
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) {
        return apiClient
                .fetchPaymentStatus(paymentRequest)
                .toTinkPayment(paymentRequest.getPayment());
    }

    @Override
    public PaymentListResponse fetchMultiple(PaymentListRequest paymentListRequest) {

        return new PaymentListResponse(
                Optional.ofNullable(paymentListRequest)
                        .map(PaymentListRequest::getPaymentRequestList)
                        .map(Collection::stream)
                        .orElseGet(Stream::empty)
                        .map(this::fetch)
                        .collect(Collectors.toList()));
    }
}
