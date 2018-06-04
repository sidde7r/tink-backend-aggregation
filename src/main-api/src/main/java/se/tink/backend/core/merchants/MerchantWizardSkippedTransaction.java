package se.tink.backend.core.merchants;

import java.util.Date;
import java.util.UUID;

public class MerchantWizardSkippedTransaction {

    private UUID userId;

    private UUID transactionId;

    private Date inserted;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public Date getInserted() {
        return inserted;
    }

    public void setInserted(Date inserted) {
        this.inserted = inserted;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(UUID transactionId) {
        this.transactionId = transactionId;
    }
}


