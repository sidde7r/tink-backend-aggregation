package se.tink.backend.aggregation.agents.utils.berlingroup.fetcher.entities;

import lombok.Getter;
import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class FetcherLinksEntity {
    private Href balances;
    private Href download;
    private Href next;
}
