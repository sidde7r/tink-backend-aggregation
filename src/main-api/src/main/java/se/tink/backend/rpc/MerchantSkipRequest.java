package se.tink.backend.rpc;

import io.protostuff.Tag;

import java.util.List;

public class MerchantSkipRequest {

    @Tag(1)
    private List<String> transactionIds;

    public List<String> getTransactionIds() {
        return transactionIds;
    }

    public void setTransactionIds(List<String> transactionIds) {
        this.transactionIds = transactionIds;
    }
}
