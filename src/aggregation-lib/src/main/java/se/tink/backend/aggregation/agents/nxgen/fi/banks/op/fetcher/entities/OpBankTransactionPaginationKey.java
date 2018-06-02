package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OpBankTransactionPaginationKey {
    private String startDate;
    private String timestamp;

    public OpBankTransactionPaginationKey(String startDate, String timestamp) {
        this.startDate = startDate;
        this.timestamp = timestamp;
    }

    public String getStartDate() {
        return startDate;
    }

    public OpBankTransactionPaginationKey setStartDate(String startDate) {
        this.startDate = startDate;
        return this;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public OpBankTransactionPaginationKey setTimestamp(String timestamp) {
        this.timestamp = timestamp;
        return this;
    }
}
