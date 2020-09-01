package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.transactionalaccount.entities;

import java.util.Date;

public class PaginationKey {
    private String formId;
    private String source;
    private String viewState;
    private long consecutiveEmptyReplies;
    private Date previousPageDate;

    public PaginationKey() {}

    public PaginationKey(
            String formId,
            String source,
            String viewState,
            long consecutiveEmptyReplies,
            Date previousPageDate) {
        this.formId = formId;
        this.source = source;
        this.viewState = viewState;
        this.consecutiveEmptyReplies = consecutiveEmptyReplies;
        this.previousPageDate = previousPageDate;
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

    public Date getPreviousPageDate() {
        return previousPageDate;
    }
}
