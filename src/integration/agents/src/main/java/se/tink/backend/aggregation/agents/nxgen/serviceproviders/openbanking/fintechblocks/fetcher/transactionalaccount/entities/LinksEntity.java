package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {

    @JsonProperty("First")
    private String first;

    @JsonProperty("Last")
    private String last;

    @JsonProperty("Next")
    private String next;

    @JsonProperty("Prev")
    private String prev;

    @JsonProperty("Self")
    private String self;

    public boolean hasNext() {
        return Optional.ofNullable(next).filter(n -> !n.equalsIgnoreCase("")).isPresent();
    }
}
