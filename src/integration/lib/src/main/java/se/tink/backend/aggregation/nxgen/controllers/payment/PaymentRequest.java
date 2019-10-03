package se.tink.backend.aggregation.nxgen.controllers.payment;

import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.backend.aggregation.rpc.TransferRequest;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payment.rpc.Reference;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.Transfer;
import se.tink.libraries.uuid.UUIDUtils;

public class PaymentRequest {
    private Payment payment;
    private Storage storage;

    public PaymentRequest(Payment payment) {
        this.payment = payment;
        this.storage = new Storage();
    }

    public PaymentRequest(Payment payment, Storage storage) {
        this.payment = payment;
        this.storage = Storage.copyOf(storage);
    }

    public static PaymentRequest of(PaymentResponse paymentResponse) {
        return new PaymentRequest(
                paymentResponse.getPayment(), Storage.copyOf(paymentResponse.getStorage()));
    }

    @Deprecated
    public static PaymentRequest of(TransferRequest transferRequest) {
        Transfer transfer = transferRequest.getTransfer();

        Creditor creditorInRequest =
                new Creditor(
                        transfer.getDestination(),
                        transfer.getDestination().getName().orElse(null));

        Reference referenceInRequest = null;
        if (TransferType.PAYMENT.equals(transfer.getType())
                || TransferType.EINVOICE.equals(transfer.getType())
                || TransferType.BANK_TRANSFER.equals(transfer.getType())) {
            referenceInRequest =
                    new Reference(transfer.getType().toString(), transfer.getDestinationMessage());
        }

        Payment.Builder paymentInRequestBuilder =
                new Payment.Builder()
                        .withCreditor(creditorInRequest)
                        .withAmount(transfer.getAmount())
                        .withExecutionDate(DateUtils.toJavaTimeLocalDate(transfer.getDueDate()))
                        .withCurrency(transfer.getAmount().getCurrency())
                        .withReference(referenceInRequest)
                        .withUniqueId(UUIDUtils.toTinkUUID(transfer.getId()));

        if (transferRequest.isTriggerRefresh()) {
            paymentInRequestBuilder.withDebtor(new Debtor(transfer.getSource()));
        }

        return new PaymentRequest(paymentInRequestBuilder.build());
    }

    public Payment getPayment() {
        return payment;
    }

    public Storage getStorage() {
        return Storage.copyOf(storage);
    }
}
