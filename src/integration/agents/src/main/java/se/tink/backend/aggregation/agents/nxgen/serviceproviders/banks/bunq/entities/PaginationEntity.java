package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PaginationEntity {
    @JsonProperty("future_url")
    private String futureUrl;

    @JsonProperty("newer_url")
    private String newerUrl;

    @JsonProperty("older_url")
    private String olderUrl;
}
