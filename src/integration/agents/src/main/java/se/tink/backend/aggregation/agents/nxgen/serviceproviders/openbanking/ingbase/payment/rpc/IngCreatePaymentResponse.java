package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment.entities.IngPaymentsLinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class IngCreatePaymentResponse {

    private String paymentId;
    private String transactionStatus;

    @JsonProperty("_links")
    private IngPaymentsLinksEntity links;
}
