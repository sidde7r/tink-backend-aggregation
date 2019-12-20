package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.loan.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PaginationKey {
    private String source;
    private String viewState;
    private int offset;
    private int skip;

    @JsonCreator
    public PaginationKey(
            @JsonProperty("source") String source,
            @JsonProperty("viewState") String viewState,
            @JsonProperty("offset") int offset,
            @JsonProperty("skip") int skip) {
        this.source = source;
        this.viewState = viewState;
        this.offset = offset;
        this.skip = skip;
    }

    public int getOffset() {
        return offset;
    }

    public int getSkip() {
        return skip;
    }

    public String getSource() {
        return source;
    }

    public String getViewState() {
        return viewState;
    }
}
