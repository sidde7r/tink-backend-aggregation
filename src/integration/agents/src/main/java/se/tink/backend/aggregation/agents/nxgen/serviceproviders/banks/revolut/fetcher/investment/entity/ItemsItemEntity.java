package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.investment.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ItemsItemEntity {

    @JsonProperty("createdAt")
    private long createdAt;

    @JsonProperty("completedAt")
    private long completedAt;

    @JsonProperty("side")
    private String side;

    @JsonProperty("id")
    private String id;

    @JsonProperty("state")
    private String state;

    @JsonProperty("type")
    private String type;

    @JsonProperty("value")
    private ValueEntity value;

    @JsonProperty("holdingId")
    private String holdingId;

    @JsonProperty("assetType")
    private String assetType;
}
