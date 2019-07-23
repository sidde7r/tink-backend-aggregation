package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.SibsAccountReferenceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.SibsAmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.dictionary.SibsPaymentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.rpc.SibsPaymentInitiationRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.sign.SignPaymentStrategy;
import se.tink.backend.aggregation.nxgen.controllers.payment.*;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.libraries.payment.rpc.Payment;

public class SibsPaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {

    private SibsBaseApiClient apiClient;
    private SignPaymentStrategy signPaymentStrategy;

    public SibsPaymentExecutor(
            SibsBaseApiClient apiClient, SignPaymentStrategy signPaymentStrategy) {
        this.apiClient = apiClient;
        this.signPaymentStrategy = signPaymentStrategy;
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
        SibsPaymentInitiationRequest sibsPaymentRequest =
                new SibsPaymentInitiationRequest.Builder()
                        .withCreditorAccount(fromCreditor(paymentRequest.getPayment()))
                        .withDebtorAccount(fromDebtor(paymentRequest.getPayment()))
                        .withInstructedAmount(
                                SibsAmountEntity.of(paymentRequest.getPayment().getAmount()))
                        .withCreditorName(paymentRequest.getPayment().getCreditor().getName())
                        .build();
        String state = UUID.randomUUID().toString().replace("-", "");
        return apiClient
                .createPayment(sibsPaymentRequest, getPaymentType(paymentRequest), state)
                .toTinkPaymentResponse(paymentRequest, state);
    }

    private SibsAccountReferenceEntity fromCreditor(Payment payment) {
        return SibsAccountReferenceEntity.of(
                () -> payment.getCreditor().getAccountIdentifierType(),
                () -> payment.getCreditor().getAccountNumber());
    }

    private SibsAccountReferenceEntity fromDebtor(Payment payment) {
        return SibsAccountReferenceEntity.of(
                () -> payment.getDebtor().getAccountIdentifierType(),
                () -> payment.getDebtor().getAccountNumber());
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) throws PaymentException {
        return apiClient
                .getPayment(
                        paymentRequest.getPayment().getUniqueId(), getPaymentType(paymentRequest))
                .toTinkPaymentResponse(paymentRequest.getStorage());
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {
        return signPaymentStrategy.sign(
                paymentMultiStepRequest, getPaymentType(paymentMultiStepRequest));
    }

    @Override
    public CreateBeneficiaryMultiStepResponse createBeneficiary(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest) {
        throw new NotImplementedException(
                "createBeneficiary not yet implemented for " + this.getClass().getName());
    }

    @Override
    public PaymentResponse cancel(PaymentRequest paymentRequest) {
        return apiClient
                .cancelPayment(
                        paymentRequest.getPayment().getUniqueId(), getPaymentType(paymentRequest))
                .toTinkResponse();
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

    protected SibsPaymentType getPaymentType(PaymentRequest paymentRequest) {
        if (paymentRequest.getPayment().isSepa()) {
            return SibsPaymentType.SEPA_CREDIT_TRANSFERS;
        } else {
            return SibsPaymentType.CROSS_BORDER_CREDIT_TRANSFERS;
        }
    }
}
