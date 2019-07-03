package se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.executor.payment;

import java.time.format.DateTimeFormatter;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.BawagApiClient;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.executor.payment.entity.CreditorAccountRequest;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.executor.payment.entity.DebtorAccountRequest;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.executor.payment.entity.InstructedAmountRequest;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.executor.payment.rpc.CreatePaymentRequest;
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

public class BawagPaymentExecutor implements PaymentExecutor {

    private final BawagApiClient apiClient;

    public BawagPaymentExecutor(BawagApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) {

        CreditorAccountRequest creditorAccount =
                CreditorAccountRequest.builder()
                        .iban(paymentRequest.getPayment().getCreditor().getAccountNumber())
                        .build();

        DebtorAccountRequest debtorAccount =
                DebtorAccountRequest.builder()
                        .iban(paymentRequest.getPayment().getDebtor().getAccountNumber())
                        .build();

        InstructedAmountRequest instructedAmount =
                InstructedAmountRequest.builder()
                        .amount(paymentRequest.getPayment().getAmount().doubleValue())
                        .currency(paymentRequest.getPayment().getCurrency())
                        .build();

        CreatePaymentRequest payment =
                CreatePaymentRequest.builder()
                        .creditorAccount(creditorAccount)
                        .debtorAccount(debtorAccount)
                        .instructedAmount(instructedAmount)
                        .creditorName(paymentRequest.getPayment().getCreditor().getName())
                        .requestedExecutionDate(
                                paymentRequest
                                        .getPayment()
                                        .getExecutionDate()
                                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                        .isSepa(paymentRequest.getPayment().isSepa())
                        .build();

        return apiClient
                .createPayment(payment)
                .toTinkPayment(
                        creditorAccount,
                        debtorAccount,
                        paymentRequest.getPayment().getAmount(),
                        paymentRequest.getPayment().getExecutionDate(),
                        paymentRequest.getPayment().getType());
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) {
        return apiClient
                .fetchPayment(paymentRequest)
                .toTinkPayment(paymentRequest.getPayment().getType());
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
        throw new NotImplementedException(
                "fetchMultiple not yet implemented for " + this.getClass().getName());
    }
}
