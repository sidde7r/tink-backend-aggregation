package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.payment;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.SamlinkApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.payment.entity.CreditorAccountRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.payment.entity.CreditorAddress;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.payment.entity.DebtorAccountRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.payment.entity.InstructedAmountRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.payment.rpc.FetchPaymentResponse;
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

public class SamlinkPaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {

    private final SamlinkApiClient apiClient;

    public SamlinkPaymentExecutor(SamlinkApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) {
        final Payment payment = paymentRequest.getPayment();
        CreditorAccountRequest creditorAccount =
                new CreditorAccountRequest(payment.getCreditor().getAccountNumber());

        DebtorAccountRequest debtorAccount =
                new DebtorAccountRequest(payment.getDebtor().getAccountNumber());

        // TODO: Change when functionality to pass the address from the test is added
        CreditorAddress creditorAddress = new CreditorAddress("Helsinki", "FI");

        InstructedAmountRequest instructedAmount =
                InstructedAmountRequest.builder()
                        .amount(payment.getAmount().getValue().toString())
                        .currency(payment.getCurrency())
                        .build();

        CreatePaymentRequest createPaymentRequest =
                CreatePaymentRequest.builder()
                        .creditorAccount(creditorAccount)
                        .debtorAccount(debtorAccount)
                        .creditorAddress(creditorAddress)
                        .instructedAmount(instructedAmount)
                        .creditorName(payment.getCreditor().getName())
                        .isSepa(payment.isSepa())
                        .remittanceInformationUnstructured(payment.getReference().getValue())
                        .build();

        CreatePaymentResponse response =
                createPaymentRequest.isSepa()
                        ? apiClient.createSepaPayment(createPaymentRequest)
                        : apiClient.createForeignPayment(createPaymentRequest);

        return response.toTinkPayment(
                creditorAccount,
                debtorAccount,
                payment.getAmount(),
                payment.getExecutionDate(),
                payment.isSepa());
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) {
        FetchPaymentResponse response =
                paymentRequest.getPayment().isSepa()
                        ? apiClient.fetchSepaPayment(paymentRequest)
                        : apiClient.fetchForeignPayment(paymentRequest);

        return response.toTinkPayment(paymentRequest.getPayment().isSepa());
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
