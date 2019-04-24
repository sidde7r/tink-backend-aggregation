package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collector;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentListResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;

public abstract class NordeaBasePaymentExecutorSelector implements PaymentExecutor {

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) {
        return getRelevantExecutor(paymentRequest).create(paymentRequest);
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) {
        return getRelevantExecutor(paymentRequest).fetch(paymentRequest);
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest) {
        return getRelevantExecutor(paymentMultiStepRequest).sign(paymentMultiStepRequest);
    }

    @Override
    public CreateBeneficiaryMultiStepResponse createBeneficiary(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest) {
        throw new NotImplementedException(
                "createBeneficiary not yet implemented for " + this.getClass().getName());
    }

    @Override
    public PaymentResponse cancel(PaymentRequest paymentRequest) {
        return getRelevantExecutor(paymentRequest).cancel(paymentRequest);
    }

    @Override
    public PaymentListResponse fetchMultiple(PaymentRequest paymentRequest) {
        Collector<PaymentListResponse, ArrayList<PaymentResponse>, PaymentListResponse>
                paymentListResponseCollector =
                        Collector.of(
                                () -> new ArrayList<>(),
                                (paymentResponses, paymentListResponse) ->
                                        paymentResponses.addAll(
                                                paymentListResponse.getPaymentResponseList()),
                                (paymentResponses1, paymentResponses2) -> {
                                    paymentResponses1.addAll(paymentResponses2);
                                    return paymentResponses1;
                                },
                                paymentResponses -> new PaymentListResponse(paymentResponses));

        return getAllExecutors().stream()
                .map(executor -> executor.fetchMultiple(null))
                .collect(paymentListResponseCollector);
    }

    protected abstract PaymentExecutor getRelevantExecutor(PaymentRequest paymentRequest);

    protected abstract Collection<PaymentExecutor> getAllExecutors();
}
