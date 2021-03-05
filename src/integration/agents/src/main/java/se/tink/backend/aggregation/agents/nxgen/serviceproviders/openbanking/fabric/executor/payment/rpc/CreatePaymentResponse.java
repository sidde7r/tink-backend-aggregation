package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.executor.payment.entities.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.executor.payment.enums.FabricPaymentStatus;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreatePaymentResponse {

    private String transactionStatus;
    private String paymentId;

    private String psuMessage;
    private String scaStatus;

    @JsonProperty("_links")
    private LinksEntity links;

    private String fundsAvailable;

    @JsonIgnore
    public PaymentResponse toTinkPaymentResponse(Payment tinkPayment) {
        tinkPayment.setStatus(
                FabricPaymentStatus.mapToTinkPaymentStatus(
                        FabricPaymentStatus.fromString(transactionStatus)));
        if (paymentId != null) {
            tinkPayment.setUniqueId(paymentId); // bank Unique payment Id
        }

        return new PaymentResponse(tinkPayment);
    }
}
