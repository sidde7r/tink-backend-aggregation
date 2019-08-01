package se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.executor.payment.entities.PaymentLinksEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.executor.payment.enums.SantanderPaymentStatus;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
public class CreatePaymentResponse {

    private String transactionStatus;
    private String paymentId;
    private String psuMessage;
    private String tppMessages;

    @JsonProperty("_links")
    private PaymentLinksEntity links;

    public String getTransactionStatus() {
        return transactionStatus;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public String getSelfLink() {
        return links.getSelf();
    }

    @JsonIgnore
    public PaymentResponse toTinkPayment(Debtor debtor, Creditor creditor, PaymentType type) {
        Payment.Builder buildingPaymentResponse =
                new Payment.Builder()
                        .withCreditor(creditor)
                        .withDebtor(debtor)
                        .withUniqueId(paymentId)
                        .withStatus(
                                SantanderPaymentStatus.fromString(transactionStatus)
                                        .getPaymentStatus())
                        .withType(type);

        Payment tinkPayment = buildingPaymentResponse.build();

        return new PaymentResponse(tinkPayment);
    }
}
