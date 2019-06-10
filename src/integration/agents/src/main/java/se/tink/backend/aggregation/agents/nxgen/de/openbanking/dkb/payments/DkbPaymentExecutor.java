package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.payments;

import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbConstants.PaymentProducts;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.payments.entities.CreditorAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.payments.entities.DebtorAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.payments.entities.InstructedAmountEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.payments.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentListRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentListResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;

public class DkbPaymentExecutor implements PaymentExecutor {
    private DkbApiClient apiClient;

    public DkbPaymentExecutor(DkbApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {

        CreditorAccountEntity creditorAccountEntity = CreditorAccountEntity.of(paymentRequest);
        DebtorAccountEntity debtorAccountEntity = DebtorAccountEntity.of(paymentRequest);
        InstructedAmountEntity instructedAmountEntity = InstructedAmountEntity.of(paymentRequest);

        String paymentProduct =
                paymentRequest.getPayment().isSepa()
                        ? PaymentProducts.INSTANT_SEPA_CREDIT_TRANSFER
                        : PaymentProducts.CROSS_BORDER_CREDIT_TRANSFERS;

        CreatePaymentRequest createPaymentRequest =
                new CreatePaymentRequest.Builder()
                        .withCreditorAccount(creditorAccountEntity)
                        .withDebtorAccount(debtorAccountEntity)
                        .withInstructedAmount(instructedAmountEntity)
                        .withCreditorName(paymentRequest.getPayment().getCreditor().getName())
                        .build();

        return apiClient
                .createPayment(createPaymentRequest, paymentProduct)
                .toTinkPaymentResponse(paymentRequest);
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) throws PaymentException {
        String paymentProduct =
                paymentRequest.getPayment().isSepa()
                        ? PaymentProducts.INSTANT_SEPA_CREDIT_TRANSFER
                        : PaymentProducts.CROSS_BORDER_CREDIT_TRANSFERS;
        String paymentId =
                paymentRequest
                        .getPayment()
                        .getUniqueId()
                        .replaceAll(
                                " ", "%20"); // replace all the spaces in payment product with %20
        return apiClient.getPayment(paymentId, paymentProduct).toTinkPaymentResponse();
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {
        throw new NotImplementedException("sign not implemented for " + this.getClass().getName());
    }

    @Override
    public CreateBeneficiaryMultiStepResponse createBeneficiary(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest) {
        throw new NotImplementedException(
                "createBeneficiary not implemented for " + this.getClass().getName());
    }

    @Override
    public PaymentResponse cancel(PaymentRequest paymentRequest) {
        return null;
    }

    @Override
    public PaymentListResponse fetchMultiple(PaymentListRequest paymentListRequest)
            throws PaymentException {
        throw new NotImplementedException(
                "fetchMultiple not implemented for " + this.getClass().getName());
    }
}
