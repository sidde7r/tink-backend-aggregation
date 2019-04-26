package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.executor.payment;

import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.NordeaSeConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.entities.CreditorEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.entities.DebtorEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.enums.NordeaPaymentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.rpc.ConfirmPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.payment.*;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;

public class NordeaSeDomesticPaymentExecutor implements PaymentExecutor {
    private NordeaBaseApiClient apiClient;

    public NordeaSeDomesticPaymentExecutor(NordeaBaseApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
        CreditorEntity creditorEntity = CreditorEntity.of(paymentRequest);

        DebtorEntity debtorEntity = DebtorEntity.of(paymentRequest);

        CreatePaymentRequest createPaymentRequest =
                new CreatePaymentRequest.Builder()
                        .withAmount(paymentRequest.getPayment().getAmount().doubleValue())
                        .withCreditor(creditorEntity)
                        .withCurrency(paymentRequest.getPayment().getCurrency())
                        .withDebtor(debtorEntity)
                        .build();
        return apiClient
                .createPayment(createPaymentRequest)
                .toTinkPaymentResponse(PaymentType.DOMESTIC);
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) throws PaymentException {
        return apiClient
                .getPayment(paymentRequest.getPayment().getUniqueId())
                .toTinkPaymentResponse(PaymentType.DOMESTIC);
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {
        PaymentStatus paymentStatus;
        String nextStep;
        List fields = new ArrayList<>();
        switch (paymentMultiStepRequest.getStep()) {
            case AuthenticationStepConstants.STEP_INIT:
                ConfirmPaymentResponse confirmPaymentsResponse =
                        apiClient.confirmPayment(
                                paymentMultiStepRequest.getPayment().getUniqueId(), true);
                paymentStatus =
                        NordeaPaymentStatus.mapToTinkPaymentStatus(
                                NordeaPaymentStatus.fromString(
                                        confirmPaymentsResponse
                                                .getPaymentResponse()
                                                .getPaymentStatus()));
                nextStep = NordeaSeConstants.NordeaSignSteps.SAMPLE_STEP;
                break;
            case NordeaSeConstants.NordeaSignSteps.SAMPLE_STEP:
                paymentStatus =
                        sampleStepNordeaAutoSignsAfterAFewSeconds(
                                paymentMultiStepRequest.getPayment().getUniqueId());
                nextStep = AuthenticationStepConstants.STEP_FINALIZE;
                break;

            default:
                throw new IllegalStateException(
                        String.format("Unknown step %s", paymentMultiStepRequest.getStep()));
        }

        Payment payment = paymentMultiStepRequest.getPayment();
        payment.setStatus(paymentStatus);
        return new PaymentMultiStepResponse(payment, nextStep, fields);
    }

    @Override
    public CreateBeneficiaryMultiStepResponse createBeneficiary(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest) {
        throw new NotImplementedException(
                "createBeneficiary not yet implemented for " + this.getClass().getName());
    }

    private PaymentStatus sampleStepNordeaAutoSignsAfterAFewSeconds(String providerId)
            throws PaymentException {
        // Should be enough to get the payment auto signed.
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        GetPaymentResponse paymentResponse = apiClient.getPayment(providerId);
        return NordeaPaymentStatus.mapToTinkPaymentStatus(
                NordeaPaymentStatus.fromString(paymentResponse.getResponse().getPaymentStatus()));
    }

    @Override
    public PaymentResponse cancel(PaymentRequest paymentRequest) {
        throw new NotImplementedException(
                "cancel not yet implemented for " + this.getClass().getName());
    }

    @Override
    public PaymentListResponse fetchMultiple(PaymentRequest paymentRequest)
            throws PaymentException {
        return apiClient.fetchPayments().toTinkPaymentListResponse(PaymentType.DOMESTIC);
    }
}
