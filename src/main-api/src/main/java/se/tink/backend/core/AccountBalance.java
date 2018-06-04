package se.tink.backend.core;

import com.google.common.base.MoreObjects;
import java.util.UUID;

public class AccountBalance {
    private UUID accountId;
    private Double balance;
    private Integer date;
    private Long inserted;
    private UUID userId;

    public UUID getAccountId() {
        return accountId;
    }

    public Double getBalance() {
        return balance;
    }

    public Integer getDate() {
        return date;
    }

    public Long getInserted() {
        return inserted;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public void setDate(Integer date) {
        this.date = date;
    }

    public void setInserted(Long inserted) {
        this.inserted = inserted;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass())
                .add("userId", getUserId())
                .add("accountId", getAccountId())
                .add("date", getDate())
                .add("balance", getBalance())
                .add("inserted", getInserted())
                .toString();
    }
}
