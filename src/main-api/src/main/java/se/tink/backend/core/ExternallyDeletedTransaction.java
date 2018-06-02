package se.tink.backend.core;

import java.util.Date;
import java.util.UUID;
import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;
import se.tink.libraries.uuid.UUIDUtils;

@Deprecated
@Table(value = "externally_deleted_transactions")
public class ExternallyDeletedTransaction {

    @PrimaryKeyColumn(ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private UUID userId;

    @PrimaryKeyColumn(ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    private UUID accountId;

    @PrimaryKeyColumn(ordinal = 2, type = PrimaryKeyType.CLUSTERED)
    private String externalTransactionId;

    private Date date;

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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
