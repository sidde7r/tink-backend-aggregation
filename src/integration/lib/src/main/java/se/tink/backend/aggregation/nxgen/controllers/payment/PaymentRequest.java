package se.tink.backend.aggregation.nxgen.controllers.payment;

import com.google.common.collect.ImmutableMap;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.backend.aggregation.rpc.TransferRequest;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payment.rpc.Reference;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.Transfer;

public class PaymentRequest {
    private Payment payment;
    private Storage paymentStorage;

    public PaymentRequest(Payment payment) {
        this.payment = payment;
        this.paymentStorage = new Storage();
    }

    public PaymentRequest(Payment payment, Storage paymentStorage) {
        this.payment = payment;
        this.paymentStorage = Storage.copyOf(paymentStorage);
    }

    public Payment getPayment() {
        return payment;
    }

    public ImmutableMap getPaymentStorage() {
        return paymentStorage.getImmutableCopy();
    }

    public static PaymentRequest of(PaymentResponse paymentResponse) {
        return new PaymentRequest(
                paymentResponse.getPayment(), Storage.copyOf(paymentResponse.getPaymentStorage()));
    }

    @Deprecated
    public static PaymentRequest of(TransferRequest transferRequest) {
        Transfer transfer = transferRequest.getTransfer();

        Creditor creditorInRequest = new Creditor(transfer.getDestination());

        Debtor debtorInRequest = new Debtor(transfer.getSource());

        Reference referenceInRequest = null;
        if (TransferType.PAYMENT.equals(transfer.getType())
                || TransferType.EINVOICE.equals(transfer.getType())) {
            referenceInRequest =
                    new Reference(transfer.getType().toString(), transfer.getDestinationMessage());
        }

        Payment paymentInRequest =
                new Payment.Builder()
                        .withCreditor(creditorInRequest)
                        .withDebtor(debtorInRequest)
                        .withAmount(transfer.getAmount())
                        .withExecutionDate(DateUtils.toJavaTimeLocalDate(transfer.getDueDate()))
                        .withCurrency(transfer.getAmount().getCurrency())
                        .withReference(referenceInRequest)
                        .build();

        return new PaymentRequest(paymentInRequest);
    }
}
