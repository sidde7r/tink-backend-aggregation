package se.tink.backend.core;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;
import se.tink.libraries.uuid.UUIDUtils;

@Table(value = "transactions")
public class CassandraTransaction implements Comparable<CassandraTransaction>, Cloneable, Serializable {
    private static final long serialVersionUID = -5375788874870894612L;
    
    private UUID accountId;
    private BigDecimal exactAmount;
    private UUID categoryId;
    private String categoryType;
    private UUID credentialsId;
    private Date date;
    private String description;
    private String formattedDescription;
    @PrimaryKeyColumn(ordinal = 1, type = PrimaryKeyType.CLUSTERED)
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
    private Boolean userModifiedAmount;
    private Boolean userModifiedCategory;
    private Boolean userModifiedDate;
    private Boolean userModifiedDescription;
    private Boolean userModifiedLocation;
    private String partsSerialized;

    public static Map<String, String> getColumnMap() {
        Map<String, String> map = Maps.newHashMap();
        map.put("categoryType", "categorytype");
        map.put("userModifiedAmount", "usermodifiedamount");
        map.put("originalDescription", "originaldescription");
        map.put("internalPayloadSerialized", "internalpayloadserialized");
        map.put("payloadSerialized", "payloadserialized");
        map.put("originalAmount", "originalamount");
        map.put("formattedDescription", "formatteddescription");
        map.put("userId", "userid");
        map.put("lastModified", "lastmodified");
        map.put("accountId", "accountid");
        map.put("credentialsId", "credentialsid");
        map.put("userModifiedDescription", "usermodifieddescription");
        map.put("userModifiedDate", "usermodifieddate");
        map.put("categoryId", "categoryid");
        map.put("originalDate", "originaldate");
        map.put("userModifiedLocation", "usermodifiedlocation");
        map.put("userModifiedCategory", "usermodifiedcategory");
        map.put("merchantId", "merchantid");
        map.put("exactAmount", "exactamount");
        map.put("exactOriginalAmount", "exactoriginalamount");

        return map;
    }

    @Override
    public int compareTo(CassandraTransaction other) {
        return id.compareTo(other.id);
    }

    public UUID getAccountId() {
        return this.accountId;
    }

    public String getTinkAccountId() {
        return UUIDUtils.toTinkUUID(accountId);
    }

    public BigDecimal getAmount() {
        return this.exactAmount;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    public String getTinkCategoryId() {
        return UUIDUtils.toTinkUUID(categoryId);
    }

    public CategoryTypes getCategoryType() {
        return CategoryTypes.valueOf(categoryType);
    }

    public UUID getCredentialsId() {
        return this.credentialsId;
    }

    public String getTinkCredentialsId() {
        return UUIDUtils.toTinkUUID(credentialsId);
    }

    public Date getDate() {
        return this.date;
    }

    public String getDescription() {
        return this.description;
    }

    public String getFormattedDescription() {
        return formattedDescription;
    }

    public UUID getId() {
        return id;
    }

    public String getTinkId() {
        return UUIDUtils.toTinkUUID(id);
    }

    public long getInserted() {
        return inserted;
    }
    
    public String getInternalPayloadSerialized() {
        return internalPayloadSerialized;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public UUID getMerchantId() {
        return merchantId;
    }

    public String getTinkMerchantId() {
        return UUIDUtils.toTinkUUID(merchantId);
    }

    public String getNotes() {
        return notes;
    }

    public BigDecimal getOriginalAmount() {
        return exactOriginalAmount;
    }

    public Date getOriginalDate() {
        return originalDate;
    }

    public String getOriginalDescription() {
        return this.originalDescription;
    }

    public String getPartsSerialized() {
        return partsSerialized;
    }

    public String getPayloadSerialized() {
        return payloadSerialized;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public TransactionTypes getType() {
        if (type == null) {
            return null;
        }
        return TransactionTypes.valueOf(type);
    }

    public UUID getUserId() {
        return userId;
    }

    public String getTinkUserId() {
        return UUIDUtils.toTinkUUID(userId);
    }

    public boolean isPending() {
        return pending;
    }

    public boolean isUserModifiedAmount() {
        return userModifiedAmount;
    }

    public boolean isUserModifiedCategory() {
        return this.userModifiedCategory;
    }

    public boolean isUserModifiedDate() {
        return userModifiedDate;
    }

    public boolean isUserModifiedDescription() {
        return this.userModifiedDescription;
    }

    public boolean isUserModifiedLocation() {
        return userModifiedLocation;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public void setAmount(BigDecimal amount) {
        this.exactAmount = amount;
    }

    public void setCategoryId(UUID category) {
        this.categoryId = category;
    }

    public void setCategoryType(CategoryTypes categoryType) {
        if (categoryType == null) {
            this.categoryType = null;
        } else {
            this.categoryType = categoryType.toString();
        }
    }

    public void setCredentialsId(UUID credentials) {
        this.credentialsId = credentials;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setFormattedDescription(String formattedDescription) {
        this.formattedDescription = formattedDescription;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setInserted(Long inserted) {
        this.inserted = inserted;
    }
    
    public void setInternalPayloadSerialized(String internalPayloadSerialized) {
        this.internalPayloadSerialized = internalPayloadSerialized;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setOriginalAmount(BigDecimal originalAmount) {
        this.exactOriginalAmount = originalAmount;
    }

    public void setOriginalDate(Date originalDate) {
        this.originalDate = originalDate;
    }

    public void setOriginalDescription(String originalDescription) {
        this.originalDescription = originalDescription;
    }

    public void setPartsSerialized(String partsSerialized) {
        this.partsSerialized = partsSerialized;
    }

    public void setPayloadSerialized(String payloadSerialized) {
        this.payloadSerialized = payloadSerialized;
    }

    public void setPending(Boolean pending) {
        this.pending = pending;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public void setUserId(UUID user) {
        this.userId = user;
    }

    public void setMerchantId(UUID merchantId) {
        this.merchantId = merchantId;
    }

    public void setType(TransactionTypes type) {
        if (type == null) {
            this.type = null;
        } else {
            this.type = type.toString();
        }
    }

    public void setUserModifiedAmount(Boolean userModifiedAmount) {
        this.userModifiedAmount = userModifiedAmount;
    }

    public void setUserModifiedCategory(Boolean userModifiedCategory) {
        this.userModifiedCategory = userModifiedCategory;
    }

    public void setUserModifiedDate(Boolean userModifiedDate) {
        this.userModifiedDate = userModifiedDate;
    }

    public void setUserModifiedDescription(Boolean userModifiedDescription) {
        this.userModifiedDescription = userModifiedDescription;
    }

    public void setUserModifiedLocation(Boolean userModifiedLocation) {
        this.userModifiedLocation = userModifiedLocation;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass()).add("id", id).add("date", date).add("amount", exactAmount)
                .add("description", description).toString();
    }
}
