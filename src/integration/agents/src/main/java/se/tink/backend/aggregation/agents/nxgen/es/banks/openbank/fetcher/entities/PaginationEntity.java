package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.vavr.control.Option;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PaginationEntity {
    @JsonProperty("nextPage")
    private NextPage nextPage;

    @JsonProperty("self")
    private Self self;

    public Option<String> getNextPage() {
        return Option.of(nextPage).flatMap(n -> Option.of(n.href));
    }

    public Option<String> getSelfPage() {
        return Option.of(nextPage).flatMap(n -> Option.of(n.href));
    }

    private static class NextPage {
        @JsonProperty("href")
        private String href;
    }

    private static class Self {
        @JsonProperty("href")
        private String href;
    }
}
