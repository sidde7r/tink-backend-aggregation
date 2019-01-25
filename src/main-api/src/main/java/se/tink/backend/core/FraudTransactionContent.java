package se.tink.backend.core;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class FraudTransactionContent extends FraudDetailsContent {

    // Having duplicate info for ID, but keeping transactionIds for legacy reasons.
    private List<String> transactionIds;
    private List<FraudTransactionEntity> transactions;
    private String payload;

    @Override
    public String generateContentId() {
        if (transactionIds == null) {
            return "null";
        }
        
        if (transactions != null && transactions.size() != 0) {

            Date date = transactions.get(0).getDate();

            return String.valueOf(Objects.hash(itemType(), contentType, date));

        } else {
            Collections.sort(transactionIds);
            return String.valueOf(Objects.hash(itemType(), transactionIds));
        }
    }

    public List<FraudTransactionEntity> getTransactions() {
        return transactions;
    }

    @Override
    public FraudTypes itemType() {
        return FraudTypes.TRANSACTION;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}
