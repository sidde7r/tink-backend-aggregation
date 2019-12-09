package se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.executor.payment;

import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.TargobankApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.executor.payment.entities.InstructedAmountEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.executor.payment.rpc.CreatePaymentRequest;
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

public class TargobankPaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {
    private TargobankApiClient apiClient;

    public TargobankPaymentExecutor(TargobankApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) {
        AccountEntity creditor =
                new AccountEntity(paymentRequest.getPayment().getCreditor().getAccountNumber());
        AccountEntity debtor =
                new AccountEntity(paymentRequest.getPayment().getDebtor().getAccountNumber());
        InstructedAmountEntity instructedAmount = InstructedAmountEntity.of(paymentRequest);

        CreatePaymentRequest createPaymentRequest =
                new CreatePaymentRequest.Builder()
                        .withCreditorAccount(creditor)
                        .withCreditorName(paymentRequest.getPayment().getCreditor().getName())
                        .withDebtorAccount(debtor)
                        .withInstructedAmount(instructedAmount)
                        .build();

        return apiClient.createPayment(createPaymentRequest).toTinkPaymentResponse();
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) {
        String uniqueId = paymentRequest.getPayment().getUniqueId();
        return apiClient
                .getPayment(uniqueId)
                .toTinkPaymentResponse(paymentRequest.getPayment().getType(), uniqueId);
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest) {
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
    public PaymentListResponse fetchMultiple(PaymentListRequest paymentListRequest) {
        List<PaymentResponse> paymentResponses = new ArrayList<>();

        paymentListRequest
                .getPaymentRequestList()
                .forEach(paymentRequest -> paymentResponses.add(fetch(paymentRequest)));
        return new PaymentListResponse(paymentResponses);
    }
}
