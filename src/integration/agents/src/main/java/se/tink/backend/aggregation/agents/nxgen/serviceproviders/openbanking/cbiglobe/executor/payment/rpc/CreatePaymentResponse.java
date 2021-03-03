package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.entities.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.enums.CbiGlobePaymentStatus;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
@SuperBuilder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreatePaymentResponse {

    private String transactionStatus;
    private String paymentId;

    private String psuAuthenticationStatus;
    private String scaStatus;

    @JsonProperty("_links")
    private LinksEntity links;

    @JsonIgnore
    public PaymentResponse toTinkPaymentResponse(Payment tinkPayment) {
        tinkPayment.setStatus(
                CbiGlobePaymentStatus.mapToTinkPaymentStatus(
                        CbiGlobePaymentStatus.fromString(transactionStatus)));
        if (tinkPayment.getUniqueId() == null) {
            tinkPayment.setUniqueId(paymentId); // bank Unique payment Id
        }
        return new PaymentResponse(tinkPayment);
    }
}
