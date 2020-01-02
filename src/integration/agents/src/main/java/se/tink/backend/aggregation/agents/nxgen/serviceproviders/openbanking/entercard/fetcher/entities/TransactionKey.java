package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionKey {
    private long startAtRowNum;
    private long stopAfterRowNum;

    public TransactionKey(long offset) {
        this.startAtRowNum = offset + 1;
        this.stopAfterRowNum = offset + 100;
    }

    public long getStartAtRowNum() {
        return startAtRowNum;
    }

    public long getStopAfterRowNum() {
        return stopAfterRowNum;
    }
}
