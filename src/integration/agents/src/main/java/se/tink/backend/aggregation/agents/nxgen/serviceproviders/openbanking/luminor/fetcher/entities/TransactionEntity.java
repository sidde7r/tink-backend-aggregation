package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class TransactionEntity {

    @JsonProperty("_links")
    public Href links;
}
