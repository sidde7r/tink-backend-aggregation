package se.tink.backend.connector.rpc;

import java.util.ArrayList;
import java.util.List;

public class TransactionBatchResponse {

    private List<IngestTransactionStatus> statuses;

    public List<IngestTransactionStatus> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<IngestTransactionStatus> statuses) {
        this.statuses = statuses;
    }

    public void addStatus(IngestTransactionStatus status) {
        if (statuses == null) {
            statuses = new ArrayList<>();
        }
        this.statuses.add(status);
    }
}
