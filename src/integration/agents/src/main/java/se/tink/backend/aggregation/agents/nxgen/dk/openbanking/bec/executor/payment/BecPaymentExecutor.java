package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.bec.executor.payment;

import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.dk.openbanking.bec.BecApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.openbanking.bec.BecConstants;
import se.tink.backend.aggregation.agents.nxgen.dk.openbanking.bec.BecConstants.PaymentTypes;
import se.tink.backend.aggregation.agents.nxgen.dk.openbanking.bec.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.openbanking.bec.executor.payment.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.openbanking.bec.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.dk.openbanking.bec.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentListResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.libraries.payment.enums.PaymentType;

public class BecPaymentExecutor implements PaymentExecutor {
    private final BecApiClient apiClient;

    public BecPaymentExecutor(BecApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
        AccountEntity creditor =
                new AccountEntity(
                        paymentRequest.getPayment().getCreditor().getAccountNumber(),
                        paymentRequest.getPayment().getCreditor().getAccountNumber().substring(4));

        AccountEntity debtor =
                new AccountEntity(
                        paymentRequest.getPayment().getDebtor().getAccountNumber(),
                        paymentRequest.getPayment().getDebtor().getAccountNumber().substring(4));

        CreatePaymentRequest createPaymentRequest =
                new CreatePaymentRequest(
                        creditor,
                        paymentRequest.getPayment().getCreditor().getName(),
                        debtor,
                        new AmountEntity(
                                paymentRequest.getPayment().getAmount().getCurrency(),
                                paymentRequest.getPayment().getAmount().getValue()));

        CreatePaymentResponse createPaymentResponse = apiClient.createPayment(createPaymentRequest);

        // Id should be paymentId from createPaymentResponse
        String paymentId = "mockedId";
        PaymentType paymentType =
                BecConstants.PAYMENT_TYPE_MAPPER
                        .translate(PaymentTypes.INSTANT_DANISH_DOMESTIC_CREDIT_TRANSFER)
                        .orElse(PaymentType.UNDEFINED);

        return apiClient.getPayment(paymentId).toTinkPayment(paymentId, paymentType);
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) throws PaymentException {
        PaymentType paymentType =
                BecConstants.PAYMENT_TYPE_MAPPER
                        .translate(PaymentTypes.INSTANT_DANISH_DOMESTIC_CREDIT_TRANSFER)
                        .orElse(PaymentType.UNDEFINED);

        return apiClient
                .getPayment(paymentRequest.getPayment().getUniqueId())
                .toTinkPayment(paymentRequest.getPayment().getUniqueId(), paymentType);
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {
        throw new NotImplementedException(
                "sign not yet implemented for " + this.getClass().getName());
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
    public PaymentListResponse fetchMultiple(PaymentRequest paymentRequest)
            throws PaymentException {
        throw new NotImplementedException(
                "fetchMultiple not yet implemented for " + this.getClass().getName());
    }
}
