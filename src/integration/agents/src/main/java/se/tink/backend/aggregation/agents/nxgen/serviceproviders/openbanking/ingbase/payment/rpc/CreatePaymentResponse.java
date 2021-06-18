package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment.entities.PaymentsLinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentResponse {

    private String paymentId;

    @JsonProperty("_links")
    private PaymentsLinksEntity links;
}
