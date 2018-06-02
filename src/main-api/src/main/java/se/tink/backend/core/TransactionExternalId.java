package se.tink.backend.core;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;
import se.tink.libraries.uuid.UUIDUtils;

@Table(value = "transactions_external_id")
public class TransactionExternalId {
    @PrimaryKeyColumn(ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private UUID userId;

    @PrimaryKeyColumn(ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    private UUID accountId;

    @PrimaryKeyColumn(ordinal = 2, type = PrimaryKeyType.CLUSTERED)
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
