package se.tink.backend.aggregation.nxgen.controllers.payment;

import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payment.rpc.Reference;
import se.tink.libraries.transfer.rpc.Transfer;
import se.tink.libraries.uuid.UUIDUtils;

public class PaymentRequest {
    private Payment payment;
    private Storage storage;
    private String endUserIp;

    public PaymentRequest(Payment payment, String endUserIp) {
        this.payment = payment;
        this.storage = new Storage();
        this.endUserIp = endUserIp;
    }

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
    public static PaymentRequest of(final Transfer transfer, final String market) {
        Creditor creditorInRequest =
                new Creditor(
                        transfer.getDestination(),
                        transfer.getDestination().getName().orElse(null));

        Reference referenceInRequest =
                new Reference(
                        transfer.getType().toString(),
                        transfer.getRemittanceInformation().getValue());

        Payment.Builder paymentInRequestBuilder =
                new Payment.Builder()
                        .withCreditor(creditorInRequest)
                        .withAmount(transfer.getAmount())
                        .withCurrency(transfer.getAmount().getCurrency())
                        .withReference(referenceInRequest)
                        .withExecutionDate(DateUtils.toJavaTimeLocalDate(transfer.getDueDate()))
                        .withUniqueId(UUIDUtils.toTinkUUID(transfer.getId()));

        if (!market.equalsIgnoreCase("GB")) {
            paymentInRequestBuilder.withDebtor(new Debtor(transfer.getSource()));
        }

        return new PaymentRequest(paymentInRequestBuilder.build(), transfer.getOriginatingUserIp());
    }

    public Payment getPayment() {
        return payment;
    }

    public Storage getStorage() {
        return Storage.copyOf(storage);
    }

    public String getEndUserIp() {
        return endUserIp;
    }

    public void setEndUserIp(String endUserIp) {
        this.endUserIp = endUserIp;
    }
}
