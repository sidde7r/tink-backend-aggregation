package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {
    @JsonProperty("curies")
    private List<EmbededEntity> curies;

    @JsonProperty("self")
    private EmbededEntity self;

    @JsonProperty("next")
    private EmbededEntity next;
}
