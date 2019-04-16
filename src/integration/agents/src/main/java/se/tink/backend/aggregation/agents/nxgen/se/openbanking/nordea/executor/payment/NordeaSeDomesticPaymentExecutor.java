package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.executor.payment;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.NordeaSeConstants;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.executor.payment.entities.CreditorEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.executor.payment.entities.DebtorEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.executor.payment.enums.NordeaPaymentStatus;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.executor.payment.rpc.ConfirmPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.executor.payment.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.executor.payment.rpc.GetPaymentsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentListResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Payment;

import java.util.ArrayList;
import java.util.List;

public class NordeaSeDomesticPaymentExecutor implements PaymentExecutor {
    private NordeaBaseApiClient apiClient;

    public NordeaSeDomesticPaymentExecutor(NordeaBaseApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) {
        CreditorEntity creditorEntity =
                CreditorEntity.of(paymentRequest.getPayment().getCreditor());

        DebtorEntity debtorEntity = DebtorEntity.of(paymentRequest.getPayment().getDebtor());

        CreatePaymentRequest createPaymentRequest =
                new CreatePaymentRequest.Builder()
                        .withAmount(paymentRequest.getPayment().getAmount().doubleValue())
                        .withCreditor(creditorEntity)
                        .withCurrency(paymentRequest.getPayment().getCurrency())
                        .withDebtor(debtorEntity)
                        .build();

        return apiClient
                .createPayment(createPaymentRequest)
                .toTinkPaymentResponse(paymentRequest.getPayment().getType());
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) {
        return apiClient
                .getPayment(paymentRequest.getPayment().getProviderId())
                .toTinkPaymentResponse(paymentRequest.getPayment().getType());
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest) {
        PaymentStatus paymentStatus;
        String nextStep;
        List fields = new ArrayList<>();
        switch (paymentMultiStepRequest.getStep()) {
            case AuthenticationStepConstants.STEP_INIT:
                ConfirmPaymentResponse confirmPaymentsResponse =
                        apiClient.confirmPayment(
                                paymentMultiStepRequest.getPayment().getProviderId(), true);
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
                                paymentMultiStepRequest.getPayment().getProviderId());
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

    private PaymentStatus sampleStepNordeaAutoSignsAfterAFewSeconds(String providerId) {
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
    public PaymentMultiStepResponse createBeneficiary() {
        return null;
    }

    @Override
    public PaymentResponse cancel(PaymentRequest paymentRequest) {
        return null;
    }

    @Override
    public PaymentListResponse fetchMultiple(PaymentRequest paymentRequest) {
        GetPaymentsResponse getPaymentsResponse = apiClient.fetchPayments();
        return null;
    }
}
