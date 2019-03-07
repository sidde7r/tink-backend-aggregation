package se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {
    @JsonProperty
    private LinksDetailsEntity next;

    public boolean hasMore() {
        return next != null && next.hasMore();
    }
}
