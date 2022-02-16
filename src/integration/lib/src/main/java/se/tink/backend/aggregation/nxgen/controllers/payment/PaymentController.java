package se.tink.backend.aggregation.nxgen.controllers.payment;

import lombok.Builder;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentException;
import se.tink.backend.aggregation.nxgen.controllers.payment.exception.PaymentControllerAgentExceptionMapper;
import se.tink.backend.aggregation.nxgen.controllers.payment.exception.PaymentControllerAgentExceptionMapper.PaymentControllerAgentExceptionMapperContext;
import se.tink.backend.aggregation.nxgen.controllers.payment.exception.PaymentControllerOldExceptionMapper;
import se.tink.backend.aggregation.nxgen.controllers.payment.validation.PaymentInitializationValidator;

@Builder
public class PaymentController {
    private final PaymentExecutor paymentExecutor;
    private final FetchablePaymentExecutor fetchablePaymentExecutor;

    private final PaymentControllerAgentExceptionMapper exceptionHandler;
    private PaymentInitializationValidator validator;

    public PaymentController(PaymentExecutor paymentExecutor) {
        this(paymentExecutor, null, new PaymentControllerOldExceptionMapper());
    }

    public PaymentController(
            PaymentExecutor paymentExecutor,
            PaymentControllerAgentExceptionMapper exceptionHandler) {
        this(paymentExecutor, null, exceptionHandler);
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

    public PaymentController(
            PaymentExecutor paymentExecutor,
            FetchablePaymentExecutor fetchablePaymentExecutor,
            PaymentControllerAgentExceptionMapper exceptionHandler,
            PaymentInitializationValidator validator) {
        this.paymentExecutor = paymentExecutor;
        this.fetchablePaymentExecutor = fetchablePaymentExecutor;
        this.exceptionHandler = exceptionHandler;
        this.validator = validator;
    }

    public PaymentResponse create(PaymentRequest paymentRequest) {
        if (validator != null) {
            validator.throwIfNotPossibleToInitialize(paymentRequest.getPayment());
        }

        try {
            return paymentExecutor.create(paymentRequest);
        } catch (AgentException agentException) {
            throw exceptionHandler
                    .tryToMapToPaymentException(
                            agentException, PaymentControllerAgentExceptionMapperContext.CREATE)
                    .orElseThrow(() -> agentException);
        }
    }

    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest) {
        try {
            return paymentExecutor.sign(paymentMultiStepRequest);
        } catch (AgentException agentException) {
            throw exceptionHandler
                    .tryToMapToPaymentException(
                            agentException, PaymentControllerAgentExceptionMapperContext.SIGN)
                    .orElseThrow(() -> agentException);
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
