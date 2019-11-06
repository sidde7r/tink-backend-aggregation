package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment;

import com.google.common.base.Strings;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.payment.DateValidationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.Currency;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.entities.CreditorAgentEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.entities.CreditorNameEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.entities.RemittanceInformationEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.enums.HandelsbankenPaymentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.enums.HandelsbankenPaymentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.rpc.ConfirmPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStepConstants;
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
import se.tink.backend.aggregation.nxgen.controllers.signing.Signer;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Payment;

public abstract class HandelsbankenBasePaymentExecutor
        implements PaymentExecutor, FetchablePaymentExecutor {
    protected final HandelsbankenBaseApiClient apiClient;

    public HandelsbankenBasePaymentExecutor(HandelsbankenBaseApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
        final Payment payment = paymentRequest.getPayment();
        final HandelsbankenPaymentType paymentProduct = getPaymentType(paymentRequest);
        final Creditor creditor = payment.getCreditor();

        validateExecutionDate(payment.getExecutionDate());
        final CreatePaymentRequest createPaymentRequest =
                new CreatePaymentRequest(
                        getCreditorAccountEntity(creditor),
                        getDebtorAccountEntity(payment),
                        AmountEntity.amountOf(paymentRequest),
                        getRemittanceInformationEntity(payment),
                        CreditorNameEntity.of(creditor.getName()),
                        getCreditorAgentEntity(creditor).orElse(null));

        final PaymentResponse paymentResponse =
                apiClient
                        .createPayment(createPaymentRequest, paymentProduct)
                        .toTinkPaymentResponse(payment, paymentProduct);

        return paymentResponse;
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) throws PaymentException {
        return apiClient
                .fetchPayment(
                        paymentRequest.getPayment().getUniqueId(),
                        getPaymentType(paymentRequest).toString())
                .toTinkPaymentResponse(paymentRequest.getPayment(), getPaymentType(paymentRequest));
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {
        Payment payment = paymentMultiStepRequest.getPayment();

        ConfirmPaymentResponse confirmPaymentsResponse =
                apiClient.confirmPayment(
                        payment.getUniqueId(), getPaymentType(paymentMultiStepRequest));

        PaymentStatus paymentStatus =
                HandelsbankenPaymentStatus.fromString(
                                confirmPaymentsResponse.getTransactionStatus())
                        .getTinkPaymentStatus();

        payment.setStatus(paymentStatus);
        return new PaymentMultiStepResponse(
                payment, AuthenticationStepConstants.STEP_FINALIZE, new ArrayList<>());
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
    public PaymentListResponse fetchMultiple(PaymentListRequest paymentListRequest)
            throws PaymentException {
        List<PaymentResponse> response = new ArrayList<>();

        for (PaymentRequest request : paymentListRequest.getPaymentRequestList()) {
            response.add(fetch(request));
        }

        return new PaymentListResponse(response);
    }

    protected abstract HandelsbankenPaymentType getPaymentType(PaymentRequest paymentRequest)
            throws PaymentException;

    protected abstract AccountEntity getDebtorAccountEntity(Payment payment);

    protected abstract AccountEntity getCreditorAccountEntity(Creditor creditor);

    protected Optional<CreditorAgentEntity> getCreditorAgentEntity(Creditor creditor) {
        return Optional.empty();
    }

    protected RemittanceInformationEntity getRemittanceInformationEntity(Payment payment) {
        final String text = Strings.emptyToNull(payment.getReference().getValue());
        return new RemittanceInformationEntity(text);
    }

    protected HandelsbankenPaymentType getSepaOrCrossCurrencyPaymentType(
            PaymentRequest paymentRequest) {

        return Currency.EURO.equalsIgnoreCase(paymentRequest.getPayment().getCurrency())
                        && paymentRequest.getPayment().isSepa()
                ? HandelsbankenPaymentType.SEPA_CREDIT_TRANSFER
                : HandelsbankenPaymentType.CROSS_CURRENCY_CREDIT_TRANSFER;
    }

    public abstract Signer getSigner();

    protected void validateExecutionDate(LocalDate date) throws DateValidationException {
        if (Objects.isNull(date) || LocalDate.now().isEqual(date)) {
            return;
        }
        throw new DateValidationException(
                "Invalid payment execution date (is not today)",
                "",
                new NotImplementedException("Specifying execution date not supported."));
    }
}
