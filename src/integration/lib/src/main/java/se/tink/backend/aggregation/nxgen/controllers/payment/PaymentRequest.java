package se.tink.backend.aggregation.nxgen.controllers.payment;

import se.tink.backend.aggregation.rpc.TransferRequest;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.payment.enums.PaymentType;
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

        Creditor creditorInRequest = new Creditor(transfer.getDestination());

        Debtor debtorInRequest = new Debtor(transfer.getSource());

        PaymentType paymentType = PaymentType.INTERNATIONAL;
        if (transfer.getSource().getType() == transfer.getDestination().getType()) {
            if (transfer.getSource().getType() == AccountIdentifier.Type.IBAN) {
                paymentType = PaymentType.SEPA;
            } else {
                paymentType = PaymentType.DOMESTIC;
            }
        }

        Payment paymentInRequest =
                new Payment.Builder()
                        .withCreditor(creditorInRequest)
                        .withDebtor(debtorInRequest)
                        .withAmount(transfer.getAmount())
                        .withExecutionDate(DateUtils.toJavaTimeLocalDate(transfer.getDueDate()))
                        .withCurrency(transfer.getAmount().getCurrency())
                        .withType(paymentType)
                        .build();

        return new PaymentRequest(paymentInRequest);
    }
}
