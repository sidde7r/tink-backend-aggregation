package se.tink.backend.core;

import com.google.common.base.MoreObjects;
import java.util.UUID;
import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;

@Table(value = "accounts_balance_history")
public class AccountBalance {
    @PrimaryKeyColumn(ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    private UUID accountId;
    private Double balance;
    private Integer date;
    @PrimaryKeyColumn(ordinal = 2, type = PrimaryKeyType.CLUSTERED)
    private Long inserted;
    @PrimaryKeyColumn(ordinal = 0, type = PrimaryKeyType.PARTITIONED)
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
