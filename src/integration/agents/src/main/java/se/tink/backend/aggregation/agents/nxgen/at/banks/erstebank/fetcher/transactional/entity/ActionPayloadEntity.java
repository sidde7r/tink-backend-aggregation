package se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.fetcher.transactional.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ActionPayloadEntity {

    @JsonProperty("state")
    private String state;

    @JsonProperty("lockReason")
    private String lockReason;
}
