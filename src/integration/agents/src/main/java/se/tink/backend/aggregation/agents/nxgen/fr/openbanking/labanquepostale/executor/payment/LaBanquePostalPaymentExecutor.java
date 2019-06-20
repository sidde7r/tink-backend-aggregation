package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.executor.payment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.LaBanquePostaleApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.executor.payment.entities.CreditTransferTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.executor.payment.entities.CreditorAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.executor.payment.entities.CreditorEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.executor.payment.entities.DebtorEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.executor.payment.entities.PaymentTypeInformationEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.executor.payment.entities.SupplementaryDataEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStepConstants;
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
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;

public class LaBanquePostalPaymentExecutor implements PaymentExecutor {
    private LaBanquePostaleApiClient apiClient;

    public LaBanquePostalPaymentExecutor(LaBanquePostaleApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) {
        CreditorEntity creditorEntity = CreditorEntity.of(paymentRequest);
        DebtorEntity debtorEntity = DebtorEntity.of(paymentRequest);
        PaymentTypeInformationEntity paymentTypeInformation =
                PaymentTypeInformationEntity.of(paymentRequest);
        List<CreditTransferTransactionEntity> creditTransferTransaction =
                CreditTransferTransactionEntity.of(paymentRequest);
        SupplementaryDataEntity supplementaryData = SupplementaryDataEntity.of(paymentRequest);
        CreditorAccountEntity creditorAccount =
                new CreditorAccountEntity(
                        paymentRequest.getPayment().getCreditor().getAccountNumber());

        CreatePaymentRequest createPaymentRequest =
                new CreatePaymentRequest.Builder()
                        .withCreditor(creditorEntity)
                        .withDebtor(debtorEntity)
                        .withCreditorAccount(creditorAccount)
                        .withPaymentInformationId(UUID.randomUUID().toString())
                        .withCreationDateTime(new Date().toString())
                        .withPaymentTypeInformation(paymentTypeInformation)
                        .withNumberOfTransactions(1)
                        .withCreditTransferTransaction(creditTransferTransaction)
                        .withSupplementaryData(supplementaryData)
                        .build();

        return apiClient
                .createPayment(createPaymentRequest)
                .toTinkPaymentResponse(PaymentType.SEPA);
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) {
        // TODO: Every payment id returns same response
        int paymentId = 42;
        return apiClient.getPayment(paymentId).toTinkPaymentResponse();
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest) {
        // TODO: Every payment id returns same response
        String paymentId = "42";
        Payment payment = apiClient.confirmPayment(paymentId).toTinkPaymentResponse().getPayment();

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
    public PaymentListResponse fetchMultiple(PaymentListRequest paymentListRequest) {
        // Not implemented on banks side done
        return new PaymentListResponse(new PaymentResponse(new Payment.Builder().build()));
    }
}
