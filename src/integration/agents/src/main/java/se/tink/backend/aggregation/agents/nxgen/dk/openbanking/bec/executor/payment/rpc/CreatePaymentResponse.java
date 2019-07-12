package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.bec.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.dk.openbanking.bec.executor.payment.entities.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreatePaymentResponse {

    @JsonProperty("_links")
    private LinksEntity links;

    private String paymentId;
    private String transactionStatus;

    public String getPaymentId() {
        return paymentId;
    }
}
