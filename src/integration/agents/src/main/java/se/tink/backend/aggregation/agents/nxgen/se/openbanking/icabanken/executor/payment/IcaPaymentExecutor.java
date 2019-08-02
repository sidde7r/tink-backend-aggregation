package se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.executor.payment;

import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.executor.payment.entities.InstructedAmountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.executor.payment.rpc.CreatePaymentRequest;
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
import se.tink.libraries.payment.enums.PaymentType;

public class IcaPaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {

    private IcaBankenApiClient apiClient;

    public IcaPaymentExecutor(IcaBankenApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) {
        AccountEntity creditorEntity = AccountEntity.creditorOf(paymentRequest);
        AccountEntity debtorEntity = AccountEntity.debtorOf(paymentRequest);
        InstructedAmountEntity instructedAmountEntity = InstructedAmountEntity.of(paymentRequest);

        CreatePaymentRequest createPaymentRequest =
                new CreatePaymentRequest.Builder()
                        .withDebtorAccount(debtorEntity)
                        .withInstructedAmount(instructedAmountEntity)
                        .withCreditorAccount(creditorEntity)
                        .withCreditorName(paymentRequest.getPayment().getCreditor().getName())
                        .build();

        return apiClient
                .createPayment(createPaymentRequest, PaymentType.SEPA)
                .toTinkPaymentResponse();
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) {
        return apiClient
                .getPayment(paymentRequest.getPayment().getUniqueId(), PaymentType.SEPA)
                .toTinkPaymentResponse();
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
        // Not implemented on banks side done
        List<PaymentResponse> paymentResponses = new ArrayList<>();

        paymentListRequest
                .getPaymentRequestList()
                .forEach(paymentRequest -> paymentResponses.add(fetch(paymentRequest)));
        return new PaymentListResponse(paymentResponses);
    }
}
