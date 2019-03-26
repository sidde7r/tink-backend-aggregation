package se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.fetcher.transactional.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardBlockingButton1Entity {

    @JsonProperty("actionPayload")
    private ActionPayloadEntity actionPayloadEntity;

    @JsonProperty("label")
    private String label;
}
