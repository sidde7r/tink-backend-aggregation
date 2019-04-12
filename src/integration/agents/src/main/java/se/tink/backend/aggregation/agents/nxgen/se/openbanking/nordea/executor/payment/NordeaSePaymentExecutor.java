package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.executor.payment;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.executor.payment.entities.CreditorEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.executor.payment.entities.DebtorEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.executor.payment.enums.NordeaAccountType;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseApiClient;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentListResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;

public class NordeaSePaymentExecutor implements PaymentExecutor {
    private NordeaBaseApiClient apiClient;

    public NordeaSePaymentExecutor(NordeaBaseApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaymentResponse createPayment(PaymentRequest paymentRequest) {

        CreditorEntity creditorEntity = assembleCreditor(paymentRequest.getPayment().getCreditor());

        DebtorEntity debtorEntity = assembleDebtor(paymentRequest.getPayment().getDebtor());

        CreatePaymentRequest createPaymentRequest =
                new CreatePaymentRequest.Builder()
                        .withAmount(paymentRequest.getPayment().getAmount().doubleValue())
                        .withCreditor(creditorEntity)
                        .withCurrency(paymentRequest.getPayment().getCurrency())
                        .withDebtor(debtorEntity)
                        .withExternalId(
                                paymentRequest.getPayment().getId().toString().substring(0, 29))
                        .build();

        CreatePaymentResponse createPaymentResponse = apiClient.createPayment(createPaymentRequest);

        return assemblePaymentResponseFromCreate(createPaymentResponse, paymentRequest.getPayment());
    }

    @Override
    public PaymentResponse fetchPaymentStatus(PaymentRequest paymentRequest) {
        return null;
    }

    @Override
    public PaymentMultiStepResponse signPayment(PaymentMultiStepRequest paymentRequest) {
        return null;
    }

    @Override
    public PaymentMultiStepResponse createBeneficiary() {
        return null;
    }

    @Override
    public PaymentResponse cancelPayment(PaymentRequest paymentRequest) {
        return null;
    }

    @Override
    public PaymentListResponse fetchPayments() {
        return null;
    }

    private CreditorEntity assembleCreditor(Creditor requestCreditor) {
        return new CreditorEntity.Builder()
                .withAccount(
                        new AccountEntity(
                                NordeaAccountType.mapToNordeaAccountType(
                                                requestCreditor.getAccountIdentifierType())
                                        .name(),
                                requestCreditor.getCurrency(),
                                requestCreditor.getAccountNumber()))
                .withMessage(requestCreditor.getMessageToCreditor())
                .withName(requestCreditor.getCreditorName())
                .build();
    }

    private DebtorEntity assembleDebtor(Debtor requestDebtor) {
        return new DebtorEntity.Builder()
                .withAccount(
                        new AccountEntity(
                                NordeaAccountType.mapToNordeaAccountType(
                                                requestDebtor.getAccountIdentifierType())
                                        .name(),
                                requestDebtor.getCurrency(),
                                requestDebtor.getAccountNumber()))
                .withMessage(requestDebtor.getOwnMessage())
                .build();
    }

    private PaymentResponse assemblePaymentResponseFromCreate(
            CreatePaymentResponse createPaymentResponse, Payment payment) {
        payment.setProviderId(createPaymentResponse.getResponse().getId());
        return new PaymentResponse(payment);
    }
}
