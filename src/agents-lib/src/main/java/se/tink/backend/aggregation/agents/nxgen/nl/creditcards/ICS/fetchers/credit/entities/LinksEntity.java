package se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {
    @JsonProperty("Self")
    private String self;
    @JsonProperty("First")
    private String first;
    @JsonProperty("Prev")
    private String prev;
    @JsonProperty("Next")
    private String next;
    @JsonProperty("Last")
    private String last;
}
