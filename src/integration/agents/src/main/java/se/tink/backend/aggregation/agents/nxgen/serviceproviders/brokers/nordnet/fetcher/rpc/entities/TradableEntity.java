package se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.fetcher.rpc.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class TradableEntity {
    @JsonProperty("market_id")
    private String marketId;

    @JsonProperty("tick_size_id")
    private String tickSizeId;

    @JsonProperty("display_order")
    private int displayOrder;

    @JsonProperty("lot_size")
    private double lotSize;

    private String identifier;
}
