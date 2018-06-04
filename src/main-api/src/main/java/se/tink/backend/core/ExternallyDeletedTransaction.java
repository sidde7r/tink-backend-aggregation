package se.tink.backend.core;

import java.util.Date;
import java.util.UUID;
import se.tink.libraries.uuid.UUIDUtils;

@Deprecated
public class ExternallyDeletedTransaction {

    private UUID userId;

    private UUID accountId;

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
