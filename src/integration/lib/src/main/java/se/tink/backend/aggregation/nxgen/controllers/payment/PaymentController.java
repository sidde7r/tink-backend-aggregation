package se.tink.backend.aggregation.nxgen.controllers.payment;

import se.tink.backend.aggregation.agents.exceptions.agent.AgentException;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentControllerAgentExceptionMapper.PaymentControllerAgentExceptionMapperContext;

public class PaymentController {
    private final PaymentExecutor paymentExecutor;
    private final FetchablePaymentExecutor fetchablePaymentExecutor;

    private final PaymentControllerAgentExceptionMapper exceptionHandler;

    public PaymentController(PaymentExecutor paymentExecutor) {
        this(paymentExecutor, null, null);
    }

    public PaymentController(
            PaymentExecutor paymentExecutor, FetchablePaymentExecutor fetchablePaymentExecutor) {
        this(paymentExecutor, fetchablePaymentExecutor, new PaymentControllerOldExceptionMapper());
    }

    public PaymentController(
            PaymentExecutor paymentExecutor,
            FetchablePaymentExecutor fetchablePaymentExecutor,
            PaymentControllerAgentExceptionMapper exceptionHandler) {
        this.paymentExecutor = paymentExecutor;
        this.fetchablePaymentExecutor = fetchablePaymentExecutor;
        this.exceptionHandler = exceptionHandler;
    }

    public PaymentResponse create(PaymentRequest paymentRequest) {
        try {
            return paymentExecutor.create(paymentRequest);
        } catch (AgentException agentException) {
            throw exceptionHandler
                    .mapToPaymentException(
                            agentException, PaymentControllerAgentExceptionMapperContext.CREATE)
                    .orElse(agentException);
        }
    }

    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest) {
        try {
            return paymentExecutor.sign(paymentMultiStepRequest);
        } catch (AgentException agentException) {
            throw exceptionHandler
                    .mapToPaymentException(
                            agentException, PaymentControllerAgentExceptionMapperContext.SIGN)
                    .orElse(agentException);
        }
    }

    public CreateBeneficiaryMultiStepResponse createBeneficiary(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest) {
        return paymentExecutor.createBeneficiary(createBeneficiaryMultiStepRequest);
    }

    public PaymentResponse cancel(PaymentRequest paymentRequest) {
        return paymentExecutor.cancel(paymentRequest);
    }

    public PaymentResponse fetch(PaymentRequest paymentRequest) {
        if (canFetch()) {
            return fetchablePaymentExecutor.fetch(paymentRequest);
        } else {
            throw new UnsupportedOperationException(
                    "This payment controller doesn't support fetching.");
        }
    }

    public PaymentListResponse fetchMultiple(PaymentListRequest paymentListRequest) {
        if (canFetch()) {
            return fetchablePaymentExecutor.fetchMultiple(paymentListRequest);
        } else {
            throw new UnsupportedOperationException(
                    "This payment controller doesn't support fetching.");
        }
    }

    public boolean canFetch() {
        return fetchablePaymentExecutor != null;
    }
}
