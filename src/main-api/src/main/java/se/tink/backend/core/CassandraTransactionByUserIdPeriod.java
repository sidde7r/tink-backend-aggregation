package se.tink.backend.core;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;
import javax.annotation.Nonnull;
import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;

/**
 * Transactions indexed by userid and period
 */
@Table(value = "transactions_by_userid_period")
public class CassandraTransactionByUserIdPeriod implements Cloneable, Comparable<CassandraTransactionByUserIdPeriod> {
    private UUID accountId;
    private BigDecimal exactAmount;
    private UUID categoryId;
    private String categoryType;
    private UUID credentialsId;
    private Date date;
    private String description;
    private String formattedDescription;
    @PrimaryKeyColumn(ordinal = 2, type = PrimaryKeyType.CLUSTERED)
    private UUID id;
    private Long inserted;
    private String internalPayloadSerialized;
    private Date lastModified;
    private UUID merchantId;
    private String notes;
    private BigDecimal exactOriginalAmount;
    private Date originalDate;
    private String originalDescription;
    private String payloadSerialized;
    private Boolean pending;
    private Long timestamp;
    private String type;

    @PrimaryKeyColumn(ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private UUID userId;
    @PrimaryKeyColumn(ordinal = 1, type = PrimaryKeyType.PARTITIONED)
    private Integer period;
    private Boolean userModifiedAmount;
    private Boolean userModifiedCategory;
    private Boolean userModifiedDate;
    private Boolean userModifiedDescription;
    private Boolean userModifiedLocation;
    private String partsSerialized;

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public BigDecimal getExactAmount() {
        return exactAmount;
    }

    public void setExactAmount(BigDecimal exactAmount) {
        this.exactAmount = exactAmount;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(UUID categoryId) {
        this.categoryId = categoryId;
    }

    public CategoryTypes getCategoryType() {
        if (categoryType == null) {
            return null;
        }
        return CategoryTypes.valueOf(categoryType);
    }

    public void setCategoryType(CategoryTypes categoryType) {
        if (categoryType == null) {
            this.categoryType = null;
        } else {
            this.categoryType = categoryType.toString();
        }
    }

    public UUID getCredentialsId() {
        return credentialsId;
    }

    public void setCredentialsId(UUID credentialsId) {
        this.credentialsId = credentialsId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFormattedDescription() {
        return formattedDescription;
    }

    public void setFormattedDescription(String formattedDescription) {
        this.formattedDescription = formattedDescription;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Long getInserted() {
        return inserted;
    }

    public void setInserted(Long inserted) {
        this.inserted = inserted;
    }

    public String getInternalPayloadSerialized() {
        return internalPayloadSerialized;
    }

    public void setInternalPayloadSerialized(String internalPayloadSerialized) {
        this.internalPayloadSerialized = internalPayloadSerialized;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public UUID getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(UUID merchantId) {
        this.merchantId = merchantId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public BigDecimal getExactOriginalAmount() {
        return exactOriginalAmount;
    }

    public void setExactOriginalAmount(BigDecimal exactOriginalAmount) {
        this.exactOriginalAmount = exactOriginalAmount;
    }

    public Date getOriginalDate() {
        return originalDate;
    }

    public void setOriginalDate(Date originalDate) {
        this.originalDate = originalDate;
    }

    public String getOriginalDescription() {
        return originalDescription;
    }

    public void setOriginalDescription(String originalDescription) {
        this.originalDescription = originalDescription;
    }

    public String getPayloadSerialized() {
        return payloadSerialized;
    }

    public void setPayloadSerialized(String payloadSerialized) {
        this.payloadSerialized = payloadSerialized;
    }

    public Boolean isPending() {
        return pending;
    }

    public void setPending(Boolean pending) {
        this.pending = pending;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public TransactionTypes getType() {
        if (type == null) {
            return null;
        }
        return TransactionTypes.valueOf(type);
    }

    public void setType(TransactionTypes type) {
        if (type == null) {
            this.type = null;
        } else {
            this.type = type.toString();
        }
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public Integer getPeriod() {
        return period;
    }

    public void setPeriod(Integer period) {
        this.period = period;
    }

    public Boolean isUserModifiedAmount() {
        return userModifiedAmount;
    }

    public void setUserModifiedAmount(Boolean userModifiedAmount) {
        this.userModifiedAmount = userModifiedAmount;
    }

    public Boolean isUserModifiedCategory() {
        return userModifiedCategory;
    }

    public void setUserModifiedCategory(Boolean userModifiedCategory) {
        this.userModifiedCategory = userModifiedCategory;
    }

    public Boolean isUserModifiedDate() {
        return userModifiedDate;
    }

    public void setUserModifiedDate(Boolean userModifiedDate) {
        this.userModifiedDate = userModifiedDate;
    }

    public Boolean isUserModifiedDescription() {
        return userModifiedDescription;
    }

    public void setUserModifiedDescription(Boolean userModifiedDescription) {
        this.userModifiedDescription = userModifiedDescription;
    }

    public Boolean isUserModifiedLocation() {
        return userModifiedLocation;
    }

    public void setUserModifiedLocation(Boolean userModifiedLocation) {
        this.userModifiedLocation = userModifiedLocation;
    }

    public String getPartsSerialized() {
        return partsSerialized;
    }

    public void setPartsSerialized(String partsSerialized) {
        this.partsSerialized = partsSerialized;
    }

    @Override
    public int compareTo(@Nonnull CassandraTransactionByUserIdPeriod other) {
        return id.compareTo(other.getId());
    }
}
