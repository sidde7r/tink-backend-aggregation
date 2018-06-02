package se.tink.backend.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import io.protostuff.Exclude;
import io.protostuff.Tag;
import io.swagger.annotations.ApiModelProperty;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import javax.persistence.Transient;
import se.tink.backend.utils.StringUtils;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.serialization.utils.SerializationUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Transaction implements Comparable<Transaction>, Cloneable {
    private static final long TWIN_FUZZY_DAY_PERIOD = TimeUnit.DAYS.toMillis(3) + TimeUnit.HOURS.toMillis(12);

    public static class Fields {
        public static final String AccountId = "accountId";
        public static final String Amount = "amount";
        public static final String CategoryId = "categoryId";
        public static final String CategoryType = "categoryType";
        public static final String CredentialsId = "credentialsId";
        public static final String Date = "date";
        public static final String Description = "description";
        public static final String Id = "id";
        public static final String Inserted = "inserted";
        public static final String MerchantId = "merchantId";
        public static final String OriginalAmount = "originalAmount";
        public static final String Payload = "payload";
        public static final String Pending = "pending";
        public static final String Tags = "tags";
        public static final String Type = "type";
        public static final String UserId = "userId";
        public static final String UserModifiedCategory = "userModifiedCategory";
        public static final String externalId = "payload.EXTERNAL_ID";
    }
    
    public static class InternalPayloadKeys {
        public static final String INCOMING_TIMESTAMP = "INCOMING_TIMESTAMP";
        // Unfortunately this name can't be changed without a data migration.
        public static final String SEB_PAYLOAD = "SEB_PAYLOAD";
        public static final String PARTNER_PAYLOAD = "PARTNER_PAYLOAD";
    }

    @Tag(1)
    @ApiModelProperty(name = "accountId", value="The internal identifier of the account that the transaction belongs to.", example = "3fe2d96efacd4dc5994404a950f238a9", required = true)
    private String accountId;
    @Modifiable
    @Tag(2)
    @ApiModelProperty(name = "amount", value="The amount of the transaction. This can be modified by the user.", example = "34.50", required = true)
    private Double amount = 0.;
    @Modifiable
    @Tag(3)
    @ApiModelProperty(name = "categoryId", value="The category of the transaction. This can be modified by the user.", example = "0e1bade6a7e3459eb794f27b7ba4cea0", required = true)
    private String categoryId;
    @Tag(4)
    @ApiModelProperty(name = "categoryType", value="The category type of the transaction.", example = "EXPENSES", required = true)
    private CategoryTypes categoryType;
    @Tag(5)
    @ApiModelProperty(name = "credentialsId", value="The internal identifier of the credentials that the transaction belongs to.", example = "65bc7a41a66e4ad1aad199bbfb3c5098", required = true)
    private String credentialsId;
    @Modifiable
    @Tag(6)
    @ApiModelProperty(name = "date", value="The date the transaction was executed. This can be modified by the user.", example = "1455740874875", required = true)
    private Date date;
    @Modifiable
    @Tag(7)
    @ApiModelProperty(name = "description", value="The description of the transaction. This can be modified by the user.", example = "Stadium Sergelg Stockholm", required = true)
    private String description;
    @Exclude
    @ApiModelProperty(name = "formattedDescription", hidden = true)
    private String formattedDescription;
    @Tag(8)
    @ApiModelProperty(name = "id", value="The internal identifier of the transaction.", example = "79c6c9c27d6e42489e888e08d27205a1", required = true)
    private String id;
    @Tag(9)
    @ApiModelProperty(name = "inserted", hidden = true)
    private long inserted;
    @Exclude
    private Map<String, String> internalPayload;
    @Tag(10)
    @ApiModelProperty(name = "lastModified", value="The date the transaction was last modified by the user.", example = "1455740874875", required = true)
    private Date lastModified;
    @Tag(20)
    @ApiModelProperty(name = "merchantId", value="The internal identifier of the merchant that the transaction belongs to. If available.", example = "ba3f9312fa7d442abde61ca419877fbf")
    private String merchantId;
    @Modifiable
    @Tag(12)
    @ApiModelProperty(name = "notes", value="A free-text field modifiable by the user. Any 'word' (whitespace separated), prefixed with a #, is considered a tag. These tags becomes searchable.", example = "Delicious #cake #wedding", required = true)
    private String notes;
    @Exclude
    @ApiModelProperty(name = "originalAmount", value="The original amount that was received from the provider, before the user changed it.", example = "34.50", required = true)
    private double originalAmount;
    @Tag(13)
    @ApiModelProperty(name = "originalDate", value="The original date that was received from the provider, before the user changed it.", example = "1455740874875", required = true)
    private Date originalDate;
    @Tag(14)
    @ApiModelProperty(name = "originalDescription", value="The original description that was received from the provider, before the user changed it.", example = "Stadium Sergelg Stockholm", required = true)
    private String originalDescription;
    @Exclude
    @Transient
    @ApiModelProperty(name = "payload", value="Meta data about the transaction, in key value format with Strings.", example = "{}")
    private Map<TransactionPayloadTypes, String> payload;
    @JsonIgnore
    @Tag(15)
    private String payloadSerialized;
    @Tag(16)
    @ApiModelProperty(name = "pending", value = "Indicates if this transaction has been settled or is still pending.", required = true)
    private boolean pending;
    @Tag(17)
    @ApiModelProperty(name = "timestamp", value="The timestamp of when the transaction was first saved to database.", example = "1464543093494", required = true)
    private long timestamp;
    @Tag(18)
    @ApiModelProperty(name = "type", value="The type of the transaction.", example = "CREDIT_CARD", allowableValues = TransactionTypes.DOCUMENTED, required = true)
    private TransactionTypes type;
    @Tag(19)
    @ApiModelProperty(name = "userId", value="The internal identifier of the user that the transaction belongs to.", example = "d9f134ee2eb44846a4e02990ecc8d32e", required = true)
    private String userId;
    @Transient
    @Tag(21)
    @ApiModelProperty(name = "upcoming", value = "Indicates if this is an upcoming transaction not booked yet.")
    private boolean upcoming;
    @Exclude
    @ApiModelProperty(name = "userModifiedAmount", hidden = true)
    private boolean userModifiedAmount;
    @Exclude
    @ApiModelProperty(name = "userModifiedCategory", hidden = true)
    private boolean userModifiedCategory;
    @Exclude
    @ApiModelProperty(name = "userModifiedDate", hidden = true)
    private boolean userModifiedDate;
    @Exclude
    @ApiModelProperty(name = "userModifiedDescription", hidden = true)
    private boolean userModifiedDescription;
    @Exclude
    @ApiModelProperty(name = "userModifiedLocation", hidden = true)
    private boolean userModifiedLocation;
    @Tag(22)
    @ApiModelProperty(name = "parts", value = "Transaction parts. Available if the transaction is divided into more than one part.", hidden = true)
    private List<TransactionPart> parts;
    @Tag(23)
    @ApiModelProperty(name = "dispensableAmount", value = "The dispensable amount of the transaction.")
    private BigDecimal dispensableAmount;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final TypeReference<HashMap<String, String>> INTERNAL_PAYLOAD_TYPE_REFERENCE = new TypeReference<HashMap<String, String>>() {

    };

    private static final TypeReference<HashMap<String, Object>> PARTNER_PAYLOAD_TYPE_REFERENCE = new TypeReference<HashMap<String, Object>>() {

    };
    
    private static final TypeReference<HashMap<TransactionPayloadTypes, String>> payloadTypeReference = new TypeReference<HashMap<TransactionPayloadTypes, String>>() {

    };

    private void generateIdIfMissing() {
        if (id == null) {
            id = StringUtils.generateUUID();
        }
    }

    public boolean canBeTwinTransfer(Transaction oldTrans) {

        // Credit card transactions are never transfers.

        if (TransactionTypes.CREDIT_CARD.equals(oldTrans.getType()) || TransactionTypes.CREDIT_CARD
                .equals(this.getType())) {
            return false;
        }

        // If transactions are large we allow netting on same account.

        if (oldTrans.getAccountId().equals(this.getAccountId()) && Math.abs(oldTrans.originalAmount) <= 50000) {
            return false;
        }

        // For large transactions, we allow a interval of the amount (interest on mortgage).

        long amountInterval = 100;
        if (Math.abs(oldTrans.originalAmount) > 50000) {
            amountInterval = 95;
        }

        // Check originalAmount and originalDate.

        long ratio = Math.round(Math.min(Math.abs(this.originalAmount), Math.abs(oldTrans.originalAmount))
                / Math.max(Math.abs(this.originalAmount), Math.abs(oldTrans.originalAmount)) * 100);

        if (Math.signum(this.originalAmount * oldTrans.originalAmount) == -1
                && Range.closed(amountInterval, 100L).contains(ratio)) {

            long time = this.originalDate.getTime();
            long oldTime = oldTrans.getOriginalDate().getTime();

            long timeDifference = time - oldTime;

            // We do not have a concrete time of transactions, but some banks set transaction time to 00:00,
            // some set a real time, which could be 18:56 and most of them don't set any time, so we set it to 12:00.
            // In this case, we can receive income earlier then expenses, so we check only date, not time
            if (DateUtils.isSameDay(originalDate, oldTrans.getOriginalDate())) {
                return true;
            }

            // Check up to 3 days in the future if originalAmount < 0

            if (this.originalAmount < 0) {
                if (timeDifference > -TWIN_FUZZY_DAY_PERIOD && timeDifference < 0) {
                    return true;
                }
            }

            // Check up to 3 days back if originalAmount > 0

            if (this.originalAmount > 0) {
                if (timeDifference < TWIN_FUZZY_DAY_PERIOD && timeDifference > 0) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public Transaction clone() {
        generateIdIfMissing();
        try {
            return (Transaction) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    @Override
    public int compareTo(Transaction other) {
        return id.compareTo(other.id);
    }

    public String getAccountId() {
        return this.accountId;
    }

    public Double getAmount() {
        return this.amount;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public CategoryTypes getCategoryType() {
        return categoryType;
    }

    public String getCredentialsId() {
        return this.credentialsId;
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

    public String getId() {
        generateIdIfMissing();
        return id;
    }

    public long getInserted() {
        return inserted;
    }
    
    @ApiModelProperty(name = "internalPayload", hidden = true)
    public Map<String, String> getInternalPayload() {
        if (internalPayload == null) {
            internalPayload = new HashMap<String, String>();
        }

        return internalPayload;
    }
    
    public String getInternalPayload(String key) {
        return getInternalPayload().get(key);
    }
    
    @ApiModelProperty(name = "internalPayloadSerialized", hidden = true)
    @JsonIgnore
    public String getInternalPayloadSerialized() {
        try {
            return OBJECT_MAPPER.writeValueAsString(getInternalPayload());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    @JsonProperty
    @ApiModelProperty(name = "partnerPayload", value = "The payload that was previously ingested on the Connector API.", example = "{}")
    public Map<String, Object> getPartnerPayload() {
        Map<String, String> internalPayload = getInternalPayload();

        if (!internalPayload.containsKey(InternalPayloadKeys.PARTNER_PAYLOAD)) {
            return Collections.emptyMap();
        }

        String partnerPayloadSerialized = internalPayload.get(InternalPayloadKeys.PARTNER_PAYLOAD);
        HashMap<String, Object> partnerPayloadMap = SerializationUtils
                .deserializeFromString(partnerPayloadSerialized, PARTNER_PAYLOAD_TYPE_REFERENCE);
        return partnerPayloadMap != null ? partnerPayloadMap : Collections.emptyMap();
    }

    @SuppressWarnings("unused")
    public void setPartnerPayload(Map<String, Object> ignored) {
        // the property is only for serialization
        // You might be tempted to add this to @JsonIgnoreProperties, however that also removes it from the documentation
    }

    public Date getLastModified() {
        return lastModified;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public String getNotes() {
        return notes;
    }

    public double getOriginalAmount() {
        return originalAmount;
    }

    public Date getOriginalDate() {
        return originalDate;
    }

    public String getOriginalDescription() {
        return this.originalDescription;
    }

    public Map<TransactionPayloadTypes, String> getPayload() {
        if (payload == null) {
            payload = new HashMap<TransactionPayloadTypes, String>();
        }

        return payload;
    }
    
    public String getInternalPayload(TransactionPayloadTypes key) {
        return getPayload().get(key);
    }

    public List<TransactionPart> getParts() {
        return parts;
    }

    public String getPayloadSerialized() {
        return payloadSerialized;
    }

    public String getPayloadValue(TransactionPayloadTypes key) {
        return getPayload().get(key);
    }

    @JsonIgnore
    public Integer transformDateToPeriod() {
        LocalDate localDate = getOriginalDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return DateUtils.getYearMonth(localDate);
    }

    public long getTimestamp() {
        return timestamp;
    }

    public TransactionTypes getType() {
        return type;
    }

    public String getUserId() {
        return userId;
    }
    
    public boolean hasInternalPayload() {
        return (internalPayload != null);
    }

    public boolean hasParts() {
        return parts != null && !parts.isEmpty();
    }

    public boolean hasPayload() {
        return !Strings.isNullOrEmpty(payloadSerialized);
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
    
    public boolean isUpcoming() {
        return upcoming;
    }
    
    public boolean isValidCategoryType(CategoryTypes categoryType) {
        if (categoryType == null) {
            return false;
        }

        if (getAmount() > 0 && categoryType.equals(CategoryTypes.EXPENSES)) {
            return false;
        }

        if (getAmount() < 0 && categoryType.equals(CategoryTypes.INCOME)) {
            return false;
        }

        return true;
    }

    public void removePayload(TransactionPayloadTypes key) {
        getPayload().remove(key);
        serializePayload();
    }
    
    private void serializePayload() {
        try {
            this.payloadSerialized = OBJECT_MAPPER.writeValueAsString(getPayload());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    /**
     * Use {@link #setCategory}; for updating both categoryId and categoryType.
     */
    @Deprecated
    public void setCategoryId(String category) {
        this.categoryId = category;
    }

    /**
     * Use {@link #setCategory}; for updating both categoryId and categoryType.
     */
    @Deprecated
    public void setCategoryType(CategoryTypes categoryType) {
        this.categoryType = categoryType;
    }

    public void setCredentialsId(String credentials) {
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

    public void setId(String id) {
        this.id = id;
    }

    public void setInserted(long inserted) {
        this.inserted = inserted;
    }
    
    public void setInternalPayload(Map<String, String> internalPayload) {
        this.internalPayload = internalPayload;
    }
    
    public void setInternalPayload(String key, String value) {
        getInternalPayload().put(key, value);
    }
    
    @JsonIgnore
    public void setInternalPayloadSerialized(String internalPayloadSerialized) {
        if (Strings.isNullOrEmpty(internalPayloadSerialized)) {
            setInternalPayload(null);
        } else {
            try {
                Map<String, String> map = OBJECT_MAPPER.readValue(internalPayloadSerialized,
                        INTERNAL_PAYLOAD_TYPE_REFERENCE);
                setInternalPayload(map);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setOriginalAmount(double originalAmount) {
        this.originalAmount = originalAmount;
    }

    public void setOriginalDate(Date originalDate) {
        this.originalDate = originalDate;
    }

    public void setOriginalDescription(String originalDescription) {
        this.originalDescription = originalDescription;
    }

    public void setPayload(Map<TransactionPayloadTypes, String> payload) {
        this.payload = payload;
        serializePayload();
    }

    public void setPayload(TransactionPayloadTypes key, String value) {
        getPayload().put(key, value);
        serializePayload();
    }

    public void setPayloadSerialized(String payloadSerialized) {
        if (Strings.isNullOrEmpty(payloadSerialized)) {
            setPayload(null);
        } else {
            try {
                Map<TransactionPayloadTypes, String> map = OBJECT_MAPPER.readValue(payloadSerialized,
                        payloadTypeReference);
                setPayload(map);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setPending(boolean pending) {
        this.pending = pending;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setType(TransactionTypes type) {
        this.type = type;
    }

    public void setUserId(String user) {
        this.userId = user;
    }

    public void setUserModifiedAmount(boolean userModifiedAmount) {
        this.userModifiedAmount = userModifiedAmount;
    }

    public void setUserModifiedCategory(boolean userModifiedCategory) {
        this.userModifiedCategory = userModifiedCategory;
    }

    public void setUserModifiedDate(boolean userModifiedDate) {
        this.userModifiedDate = userModifiedDate;
    }

    public void setUserModifiedDescription(boolean userModifiedDescription) {
        this.userModifiedDescription = userModifiedDescription;
    }

    public void setUserModifiedLocation(boolean userModifiedLocation) {
        this.userModifiedLocation = userModifiedLocation;
    }

    public void setUpcoming(boolean upcoming) {
        this.upcoming = upcoming;
    }

    /**
     * Get the dispensable amount of if exists. If no value is available then the dispensable amount is the amount of
     * the transaction.
     */
    public BigDecimal getDispensableAmount() {
        return dispensableAmount == null ? BigDecimal.valueOf(amount) : dispensableAmount;
    }

    @Override
    public String toString() {
        generateIdIfMissing();
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("credentialsId", credentialsId)
                .add("accountId", accountId)
                .add("date", getDate())
                .add("amount", getAmount())
                .add("description", getDescription())
                .toString();
    }

    /**
     * Change merchant on a transaction. Change description if it is unmodified
     * @param merchant
     */
    public void changeMerchant(Merchant merchant){
        setMerchantId(merchant.getId());

        // Update description if it is unmodified
        if (!isUserModifiedDescription()){
            setDescription(StringUtils.formatHuman(merchant.getName()));
        }
    }

    public void setCategory(Category category) {
        Preconditions.checkNotNull(category, "Category must not be null.");
        setCategory(category.getId(), category.getType());
    }

    public void setCategory(String categoryId, CategoryTypes categoryType) {
        Preconditions.checkNotNull(categoryId, "CategoryId must not be null.");
        Preconditions.checkNotNull(categoryType, "CategoryType must not be null.");

        this.categoryId = categoryId;
        this.categoryType = categoryType;
    }

    /**
     * Reset the category information on a transaction. Both the categoryId and the categoryType.
     */
    public void resetCategory() {
        this.categoryId = null;
        this.categoryType = null;
    }

    public void setParts(List<TransactionPart> parts) {
        this.dispensableAmount = null;
        this.parts = null;

        if (parts != null) {
            parts.forEach(this::addPart);
        }
    }

    public void addPart(TransactionPart part) {
        Preconditions.checkNotNull(part);

        if (parts == null) {
            parts = Lists.newArrayList();
        }

        parts.add(part);

        // Update the dispensable amount
        if (getDispensableAmount().abs().compareTo(part.getAmount().abs()) > 0) {
            dispensableAmount = getDispensableAmount().subtract(part.getAmount());
        } else {
            dispensableAmount = BigDecimal.ZERO;
        }
    }

    public boolean removePart(TransactionPart part) {
        if (parts == null || parts.isEmpty()) {
            return false;
        }

        boolean removed = parts.removeIf(x -> Objects.equals(x.getId(), part.getId()));

        if (removed) {
            dispensableAmount = getDispensableAmount().add(part.getAmount());
        }

        return removed;
    }

    public void copyToExisting(Transaction transaction) {
        setPayloadSerialized(transaction.getPayloadSerialized());

        if (isPending() && !transaction.isPending()) {
            setPayload(TransactionPayloadTypes.UNSETTLED_AMOUNT, String.valueOf(getOriginalAmount()));
        }


        setPending(transaction.isPending());
        setOriginalDescription(transaction.getOriginalDescription());
        setOriginalAmount(transaction.getOriginalAmount());
        setOriginalDate(transaction.getOriginalDate());

        if (!isUserModifiedDescription()) {
            setDescription(transaction.getDescription());
            setFormattedDescription(transaction.getOriginalDescription());
        }

        if (!isUserModifiedAmount()) {
            setAmount(transaction.getAmount());
        }

        if (!isUserModifiedCategory() && transaction.getCategoryId() != null) {
            setCategoryId(transaction.getCategoryId());
        }

        if (!isUserModifiedDate()) {
            setDate(transaction.getDate());
        }
    }

    public void copyToIncoming(Transaction transaction) {
        setPayloadSerialized(transaction.getPayloadSerialized());

        if (!isPending() && transaction.isPending()) {
            setPayload(TransactionPayloadTypes.UNSETTLED_AMOUNT, String.valueOf(transaction.getOriginalAmount()));
        }

        if (!isUserModifiedCategory() && getCategoryId() != null) {
            setCategoryId(transaction.getCategoryId());
        }
    }

    public void setDispensableAmount(BigDecimal dispensableAmount) {
        this.dispensableAmount = dispensableAmount;
    }

    public boolean isUserModified() {
        return this.isUserModifiedAmount() || this.isUserModifiedCategory() || this.isUserModifiedDate()
                || this.isUserModifiedDescription() || this.isUserModifiedLocation();
    }

}
