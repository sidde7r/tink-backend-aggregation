package se.tink.backend.aggregation.agents.utils.berlingroup.payment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.entities.LinksEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.enums.AspspPaymentStatus;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
@Getter
public class CreatePaymentResponse {

    private String transactionStatus;
    private String paymentId;
    private String psuMessage;

    @JsonProperty("_links")
    private LinksEntity links;

    public String getPaymentId() {
        return paymentId;
    }

    @JsonIgnore
    public PaymentResponse toTinkPayment(Payment tinkPayment) {
        tinkPayment.setStatus(AspspPaymentStatus.fromString(transactionStatus).getPaymentStatus());
        if (paymentId != null) {
            tinkPayment.setUniqueId(paymentId); // bank Unique payment Id
        }
        return new PaymentResponse(tinkPayment);
    }
}
