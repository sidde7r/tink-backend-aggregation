package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.entities.BasketLinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@Setter
@Getter
@JsonObject
public class CreateBasketResponse {
    @JsonProperty("_links")
    private BasketLinksEntity links;

    private String basketId;
}
