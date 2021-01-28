package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment;

import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.SebApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment.rpc.PaymentStatusResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.signing.multifactor.bankid.BankIdSigner;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Payment;

public class SebBankIdSigner implements BankIdSigner<PaymentRequest> {

    private SebApiClient apiClient;

    public SebBankIdSigner(SebApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public BankIdStatus collect(PaymentRequest toCollect) throws AuthenticationException {
        PaymentStatus paymentStatus = fetchPaymentStatus(toCollect);
        switch (paymentStatus) {
            case CREATED:
            case PENDING:
                return BankIdStatus.WAITING;

            case SIGNED:
            case PAID:
                return BankIdStatus.DONE;

            case REJECTED:
            case CANCELLED:
                return BankIdStatus.CANCELLED;

            case USER_APPROVAL_FAILED:
                return BankIdStatus.INTERRUPTED;

            case UNDEFINED:
            default:
                return BankIdStatus.FAILED_UNKNOWN;
        }
    }

    private PaymentStatus fetchPaymentStatus(PaymentRequest paymentRequest) throws BankIdException {
        final Payment payment = paymentRequest.getPayment();
        final String paymentProduct =
                SebPaymentUtil.getPaymentProduct(
                                payment.getType(), payment.getCreditor().getAccountIdentifierType())
                        .getValue();
        final String paymentId = payment.getUniqueId();

        try {
            PaymentStatusResponse paymentStatusResponse =
                    apiClient.getPaymentStatus(paymentId, paymentProduct);
            return SebPaymentStatus.mapToTinkPaymentStatus(
                    SebPaymentStatus.fromString(paymentStatusResponse.getTransactionStatus()));
        } catch (PaymentException e) {
            throw BankIdError.UNKNOWN.exception();
        }
    }
}
