package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PaginationKey {
    @JsonProperty private String formId;
    @JsonProperty private String source;
    @JsonProperty private String viewState;
    @JsonProperty private long consecutiveEmptyReplies;

    public PaginationKey() {}

    public PaginationKey(
            String formId, String source, String viewState, long consecutiveEmptyReplies) {
        this.formId = formId;
        this.source = source;
        this.viewState = viewState;
        this.consecutiveEmptyReplies = consecutiveEmptyReplies;
    }

    public String getFormId() {
        return formId;
    }

    public String getSource() {
        return source;
    }

    public String getViewState() {
        return viewState;
    }

    public long getConsecutiveEmptyReplies() {
        return consecutiveEmptyReplies;
    }
}
