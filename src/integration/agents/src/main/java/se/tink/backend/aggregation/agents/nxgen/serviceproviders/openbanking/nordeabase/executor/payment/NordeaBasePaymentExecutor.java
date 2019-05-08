package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment;

import io.vavr.CheckedFunction1;
import io.vavr.Value;
import io.vavr.control.Try;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.Field;
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
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentListResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;

public abstract class NordeaBasePaymentExecutor implements PaymentExecutor {
    private NordeaBaseApiClient apiClient;

    public NordeaBasePaymentExecutor(NordeaBaseApiClient apiClient) {
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
                .createPayment(createPaymentRequest, getPaymentType(paymentRequest))
                .toTinkPaymentResponse(getPaymentType(paymentRequest));
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) throws PaymentException {
        return apiClient
                .getPayment(
                        paymentRequest.getPayment().getUniqueId(), getPaymentType(paymentRequest))
                .toTinkPaymentResponse(getPaymentType(paymentRequest));
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {
        PaymentStatus paymentStatus;
        String nextStep;
        switch (paymentMultiStepRequest.getStep()) {
            case AuthenticationStepConstants.STEP_INIT:
                ConfirmPaymentResponse confirmPaymentsResponse =
                        apiClient.confirmPayment(
                                paymentMultiStepRequest.getPayment().getUniqueId(),
                                getPaymentType(paymentMultiStepRequest));
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
                                paymentMultiStepRequest.getPayment().getUniqueId(),
                                getPaymentType(paymentMultiStepRequest));
                nextStep = AuthenticationStepConstants.STEP_FINALIZE;
                break;

            default:
                throw new IllegalStateException(
                        String.format("Unknown step %s", paymentMultiStepRequest.getStep()));
        }

        Payment payment = paymentMultiStepRequest.getPayment();
        payment.setStatus(paymentStatus);
        return new PaymentMultiStepResponse(payment, nextStep, new ArrayList<>());
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
    public PaymentListResponse fetchMultiple(PaymentRequest paymentRequest)
            throws PaymentException {
        Collector<PaymentListResponse, ArrayList<PaymentResponse>, PaymentListResponse>
                paymentListResponseCollector =
                        Collector.of(
                                ArrayList::new,
                                (paymentResponses, paymentListResponse) ->
                                        paymentResponses.addAll(
                                                paymentListResponse.getPaymentResponseList()),
                                (paymentResponses1, paymentResponses2) -> {
                                    paymentResponses1.addAll(paymentResponses2);
                                    return paymentResponses1;
                                },
                                paymentResponses -> new PaymentListResponse(paymentResponses));

        List<Try<PaymentListResponse>> allTries =
                getSupportedPaymentTypes().stream()
                        .map(
                                CheckedFunction1.liftTry(
                                        paymentType ->
                                                apiClient
                                                        .fetchPayments(paymentType)
                                                        .toTinkPaymentListResponse(paymentType)))
                        .collect(Collectors.toList());

        List<Try<PaymentListResponse>> failedTries =
                allTries.stream().filter(Try::isFailure).collect(Collectors.toList());

        if (!failedTries.isEmpty()) {
            Throwable failedTryCause = failedTries.stream().findFirst().get().getCause();
            if (failedTryCause instanceof PaymentException) {
                throw (PaymentException) failedTryCause;
            } else {
                throw new PaymentException(
                        "Unrecognized exception when fetching multiple payments.", failedTryCause);
            }
        }

        return allTries.stream().flatMap(Value::toJavaStream).collect(paymentListResponseCollector);
    }

    protected abstract PaymentType getPaymentType(PaymentRequest paymentRequest);

    protected abstract Collection<PaymentType> getSupportedPaymentTypes();

    private PaymentStatus sampleStepNordeaAutoSignsAfterAFewSeconds(
            String providerId, PaymentType paymentType) throws PaymentException {
        // Should be enough to get the payment auto signed.
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        GetPaymentResponse paymentResponse = apiClient.getPayment(providerId, paymentType);
        return NordeaPaymentStatus.mapToTinkPaymentStatus(
                NordeaPaymentStatus.fromString(paymentResponse.getResponse().getPaymentStatus()));
    }
}
