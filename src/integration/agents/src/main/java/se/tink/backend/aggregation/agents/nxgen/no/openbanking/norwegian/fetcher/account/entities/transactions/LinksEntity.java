package se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.fetcher.account.entities.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class LinksEntity {

    @JsonProperty("next")
    private Href nextEntity;
}
