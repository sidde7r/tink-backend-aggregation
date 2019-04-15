package se.tink.backend.aggregation.nxgen.controllers.payment;

import se.tink.backend.aggregation.rpc.TransferRequest;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.rpc.Transfer;

public class PaymentRequest {
    private Payment payment;

    public PaymentRequest(Payment payment) {
        this.payment = payment;
    }

    public Payment getPayment() {
        return payment;
    }

    public static PaymentRequest of(PaymentResponse paymentResponse) {
        return new PaymentRequest(paymentResponse.getPayment());
    }

    @Deprecated
    public static PaymentRequest of(TransferRequest transferRequest) {
        Transfer transfer = transferRequest.getTransfer();

        // Assumptions for the temporary mapper:
        //  - Creditor and Debtor account currencies are the same as the transfer currency.
        Creditor creditorInRequest =
                new Creditor(
                        transfer.getDestination(),
                        transfer.getAmount().getCurrency());

        Debtor debtorInRequest =
                new Debtor(
                        transfer.getSource(),
                        transfer.getAmount().getCurrency());

        Payment paymentInRequest =
                new Payment(
                        creditorInRequest,
                        debtorInRequest,
                        transfer.getAmount(),
                        DateUtils.toJavaTimeLocalDate(transfer.getDueDate()),
                        transfer.getAmount().getCurrency());

        return new PaymentRequest(paymentInRequest);
    }
}
