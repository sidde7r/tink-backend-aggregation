package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment;

import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.Currency;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.entities.AmountEntity;
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
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Payment;

public abstract class HandelsbankenBasePaymentExecutor
        implements PaymentExecutor, FetchablePaymentExecutor {
    private final HandelsbankenBaseApiClient apiClient;
    private final List<PaymentResponse> createdPaymentList;

    public HandelsbankenBasePaymentExecutor(HandelsbankenBaseApiClient apiClient) {
        this.apiClient = apiClient;
        createdPaymentList = new ArrayList<>();
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
        AccountEntity creditor = AccountEntity.creditorOf(paymentRequest);
        AccountEntity debtor = AccountEntity.debtorOf(paymentRequest);
        AmountEntity amount = AmountEntity.amountOf(paymentRequest);

        CreatePaymentRequest createPaymentRequest =
                new CreatePaymentRequest(creditor, debtor, amount);

        HandelsbankenPaymentType paymentProduct = getPaymentType(paymentRequest);

        PaymentResponse paymentResponse =
                apiClient
                        .createPayment(createPaymentRequest, paymentProduct)
                        .toTinkPaymentResponse(paymentRequest.getPayment(), paymentProduct);

        createdPaymentList.add(paymentResponse);

        return paymentResponse;
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) throws PaymentException {
        return apiClient
                .getPayment(
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
    public PaymentListResponse fetchMultiple(PaymentListRequest paymentRequest) {
        // The API doesn't support fetching of multiple payments
        return new PaymentListResponse(createdPaymentList);
    }

    protected abstract HandelsbankenPaymentType getPaymentType(PaymentRequest paymentRequest);

    protected HandelsbankenPaymentType getSepaOrCrossCurrencyPaymentType(
            PaymentRequest paymentRequest) {
        return Currency.EURO.equalsIgnoreCase(paymentRequest.getPayment().getCurrency())
                        && paymentRequest.getPayment().isSepa()
                ? HandelsbankenPaymentType.SEPA_CREDIT_TRANSFER
                : HandelsbankenPaymentType.CROSS_CURRENCY_CREDIT_TRANSFER;
    }
}
