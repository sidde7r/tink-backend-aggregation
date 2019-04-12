package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.executor.payment;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.executor.payment.entities.CreditorEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.executor.payment.entities.DebtorEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.executor.payment.enums.NordeaAccountType;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseApiClient;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentListResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;

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
                        .withExternalId(paymentRequest.getPayment().getId().toString())
                        .build();

        apiClient.createPayment(createPaymentRequest);

        return null;
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
                                        .toString(),
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
                                        .toString(),
                                requestDebtor.getCurrency(),
                                requestDebtor.getAccountNumber()))
                .withMessage(requestDebtor.getOwnMessage())
                .build();
    }
}
