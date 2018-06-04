package se.tink.backend.core;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import se.tink.libraries.uuid.UUIDUtils;

public class TransactionExternalId {
    private UUID userId;

    private UUID accountId;

    private String externalTransactionId;

    private boolean deleted;

    private UUID transactionId;

    private Date updated;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = UUIDUtils.fromTinkUUID(userId);
    }

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = UUIDUtils.fromTinkUUID(accountId);
    }

    public String getExternalTransactionId() {
        return externalTransactionId;
    }

    public void setExternalTransactionId(String externalTransactionId) {
        this.externalTransactionId = externalTransactionId;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = UUIDUtils.fromTinkUUID(transactionId);
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public void setUpdatedToNow() {
        this.updated = Date.from(Instant.now());
    }
}
