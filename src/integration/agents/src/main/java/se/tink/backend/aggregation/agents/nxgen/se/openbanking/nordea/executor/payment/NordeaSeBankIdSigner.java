package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.executor.payment;

import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.signing.multifactor.bankid.BankIdSigner;
import se.tink.libraries.payment.enums.PaymentStatus;

public class NordeaSeBankIdSigner implements BankIdSigner<PaymentRequest> {

    private NordeaSePaymentExecutorSelector paymentExecutor;

    public NordeaSeBankIdSigner(NordeaSePaymentExecutorSelector paymentExecutor) {
        this.paymentExecutor = paymentExecutor;
    }

    @Override
    public BankIdStatus collect(PaymentRequest toCollect) throws AuthenticationException {
        try {
            PaymentStatus paymentResponse =
                    paymentExecutor.fetch(toCollect).getPayment().getStatus();

            switch (paymentResponse) {
                case CREATED:
                case PENDING:
                    return BankIdStatus.WAITING;

                case SIGNED:
                case PAID:
                case REJECTED:
                case CANCELLED:
                    return BankIdStatus.DONE;

                case USER_APPROVAL_FAILED:
                    return BankIdStatus.INTERRUPTED;

                case UNDEFINED:
                default:
                    return BankIdStatus.FAILED_UNKNOWN;
            }
        } catch (PaymentException e) {
            throw BankIdError.UNKNOWN.exception();
        }
    }
}
