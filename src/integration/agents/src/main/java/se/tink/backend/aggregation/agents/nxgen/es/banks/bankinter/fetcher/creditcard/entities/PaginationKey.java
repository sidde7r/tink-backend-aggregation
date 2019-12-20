package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PaginationKey {
    @JsonProperty private String source;
    @JsonProperty private String viewState;

    @JsonCreator
    public PaginationKey(
            @JsonProperty("source") String source, @JsonProperty("viewState") String viewState) {
        this.source = source;
        this.viewState = viewState;
    }

    public String getSource() {
        return source;
    }

    public String getViewState() {
        return viewState;
    }
}
