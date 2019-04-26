package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment;

import io.vavr.CheckedFunction1;
import io.vavr.Value;
import io.vavr.control.Try;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.nxgen.controllers.payment.*;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;

public abstract class NordeaBasePaymentExecutorSelector implements PaymentExecutor {

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
        return getRelevantExecutor(paymentRequest).create(paymentRequest);
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) throws PaymentException {
        return getRelevantExecutor(paymentRequest).fetch(paymentRequest);
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {
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
    public PaymentListResponse fetchMultiple(PaymentRequest paymentRequest)
            throws PaymentException {
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

        List<Try<PaymentListResponse>> allTries =
                getAllExecutors().stream()
                        .map(CheckedFunction1.liftTry(executor -> executor.fetchMultiple(null)))
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

    protected abstract PaymentExecutor getRelevantExecutor(PaymentRequest paymentRequest);

    protected abstract Collection<PaymentExecutor> getAllExecutors();
}
