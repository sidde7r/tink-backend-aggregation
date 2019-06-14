package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.executor.payment.entities.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PaymentAuthorisationResponse {
    private String scaStatus;

    @JsonProperty("_links")
    private LinksEntity links;

    public LinksEntity getLinks() {
        return links;
    }
}
