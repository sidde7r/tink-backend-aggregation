package se.tink.backend.aggregation.nxgen.controllers.payment;

import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.rpc.Transfer;
import se.tink.libraries.uuid.UUIDUtils;

public class PaymentRequest {
    private Payment payment;
    private Storage storage;
    private String originatingUserIp;

    public PaymentRequest(Payment payment, String originatingUserIp) {
        this.payment = payment;
        this.storage = new Storage();
        this.originatingUserIp = originatingUserIp;
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

        Payment.Builder paymentInRequestBuilder =
                new Payment.Builder()
                        .withCreditor(creditorInRequest)
                        .withAmount(transfer.getAmount())
                        .withCurrency(transfer.getAmount().getCurrency())
                        .withExecutionDate(DateUtils.toJavaTimeLocalDate(transfer.getDueDate()))
                        .withUniqueId(UUIDUtils.toTinkUUID(transfer.getId()))
                        .withRemittanceInformation(transfer.getRemittanceInformation())
                        .withPaymentScheme(transfer.getPaymentScheme());

        // If source account is optional then populate Debtor only if source is not null
        if (transfer.getSource() != null) {
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

    public String getOriginatingUserIp() {
        return originatingUserIp;
    }

    public void setOriginatingUserIp(String originatingUserIp) {
        this.originatingUserIp = originatingUserIp;
    }
}
