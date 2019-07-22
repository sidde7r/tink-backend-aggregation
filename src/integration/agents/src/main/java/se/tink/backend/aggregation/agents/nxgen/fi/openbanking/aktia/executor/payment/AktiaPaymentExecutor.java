package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.executor.payment;

import java.time.format.DateTimeFormatter;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.AktiaApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.AktiaConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.executor.payment.entities.PaymentAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.fetcher.transactionalaccount.entities.AmountEntity;
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
import se.tink.libraries.payment.rpc.Payment;

public class AktiaPaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {
    private final AktiaApiClient apiClient;

    public AktiaPaymentExecutor(AktiaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
        final Payment payment = paymentRequest.getPayment();
        final PaymentAccountEntity creditor =
                new PaymentAccountEntity(payment.getCreditor().getAccountNumber());
        final PaymentAccountEntity debtor =
                new PaymentAccountEntity(payment.getDebtor().getAccountNumber());
        final CreatePaymentRequest createPaymentRequest =
                new CreatePaymentRequest(
                        creditor,
                        payment.getCreditor().getName(),
                        debtor,
                        FormValues.END_TO_END_IDENTIFICATION,
                        new AmountEntity(payment.getAmount()),
                        FormValues.REMITTANCE_INFORMATION_UNSTRUCTURED,
                        payment.getExecutionDate()
                                .format(DateTimeFormatter.ofPattern(FormValues.DATE_FORMAT)));

        return apiClient
                .createPayment(createPaymentRequest)
                .toTinkPaymentResponse(
                        creditor, debtor, payment.getAmount(), payment.getExecutionDate());
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) throws PaymentException {
        final String paymentId = paymentRequest.getPayment().getUniqueId();

        return apiClient.getPayment(paymentId).toTinkPayment(paymentId);
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
    public PaymentListResponse fetchMultiple(PaymentListRequest paymentListRequest)
            throws PaymentException {
        throw new NotImplementedException(
                "fetchMultiple not yet implemented for " + this.getClass().getName());
    }
}
