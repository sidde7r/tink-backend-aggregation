package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment;

import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.signing.multifactor.bankid.BankIdSigner;
import se.tink.libraries.payment.enums.PaymentStatus;

public class SbabBankIdSigner implements BankIdSigner<PaymentRequest> {
    private SbabPaymentExecutor paymentExecutor;

    public SbabBankIdSigner(SbabPaymentExecutor paymentExecutor) {
        this.paymentExecutor = paymentExecutor;
    }

    @Override
    public BankIdStatus collect(PaymentRequest toCollect) throws AuthenticationException {
        PaymentStatus paymentStatus;
        try {
            paymentStatus = paymentExecutor.fetch(toCollect).getPayment().getStatus();
        } catch (PaymentException e) {
            throw BankIdError.UNKNOWN.exception();
        }

        switch (paymentStatus) {
            case CREATED:
            case PENDING:
                return BankIdStatus.WAITING;

            case SIGNED:
            case PAID:
            case REJECTED:
            case CANCELLED:
                return BankIdStatus.DONE;

            case UNDEFINED:
            default:
                return BankIdStatus.FAILED_UNKNOWN;
        }
    }
}
