package se.tink.backend.core;

import com.datastax.driver.core.utils.UUIDs;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;
import se.tink.libraries.uuid.UUIDUtils;

@Table(value = "transaction_category_change_records")
public class CategoryChangeRecord {
    private String command;
    private UUID newCategory;
    private UUID oldCategory;

    private Date timestamp;

    @PrimaryKeyColumn(ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private UUID userId;
    @PrimaryKeyColumn(ordinal = 2, type = PrimaryKeyType.CLUSTERED)
    private UUID id;
    @PrimaryKeyColumn(ordinal = 1, type = PrimaryKeyType.PARTITIONED)
    private UUID transactionId;

    public String getCommand() {
        return command;
    }

    public UUID getId() {
        return id;
    }

    public UUID getNewCategory() {
        return newCategory;
    }

    public UUID getOldCategory() {
        return oldCategory;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setNewCategory(UUID newCategory) {
        this.newCategory = newCategory;
    }

    public void setOldCategory(UUID oldCategory) {
        this.oldCategory = oldCategory;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public void setTransactionId(UUID transactionId) {
        this.transactionId = transactionId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public static CategoryChangeRecord createChangeRecord(Transaction transaction,
            Optional<String> lastCategory, String command) {
        CategoryChangeRecord change = new CategoryChangeRecord();

        change.setId(UUIDs.timeBased());

        change.setTransactionId(UUIDUtils.fromTinkUUID(transaction.getId()));
        change.setUserId((UUIDUtils.fromTinkUUID(transaction.getUserId())));

        change.setNewCategory(UUIDUtils.fromTinkUUID(transaction.getCategoryId()));
        change.setOldCategory(lastCategory.map(UUIDUtils.FROM_TINK_UUID_TRANSFORMER::apply).orElse(null));
        change.setTimestamp(new Date());
        change.setCommand(command);

        return change;
    }

}
