package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment;

import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.signing.multifactor.bankid.BankIdSigner;
import se.tink.libraries.payment.enums.PaymentStatus;

public class SebBankIdSigner implements BankIdSigner<PaymentRequest> {

    private SebPaymentExecutor paymentExecutor;

    public SebBankIdSigner(SebPaymentExecutor paymentExecutor) {
        this.paymentExecutor = paymentExecutor;
    }

    @Override
    public BankIdStatus collect(PaymentRequest toCollect) {
        PaymentStatus paymentResponse = paymentExecutor.fetchStatus(toCollect);

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
    }
}
