package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EmbededEntity {
    @JsonProperty("href")
    private String href;

    @JsonProperty("method")
    private String method;

    @JsonProperty("name")
    private String name;
}
