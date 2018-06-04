package se.tink.backend.system.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import se.tink.backend.utils.StringUtils;
import se.tink.libraries.date.DateUtils;

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
    }

    public static class InternalPayloadKeys {
        public static final String INCOMING_TIMESTAMP = "INCOMING_TIMESTAMP";
        // Unfortunately this name can't be changed without a data migration.
        public static final String SEB_PAYLOAD = "SEB_PAYLOAD";
        public static final String PARTNER_PAYLOAD = "PARTNER_PAYLOAD";
    }

    private String accountId;
    private double amount;
    private String categoryId;
    private CategoryTypes categoryType;
    private String credentialsId;
    private Date date;
    private String description;
    private String formattedDescription;
    private String id;
    private long inserted;
    private Map<String, String> internalPayload;
    private Date lastModified;
    private String merchantId;
    private String notes;
    private double originalAmount;
    private Date originalDate;
    private String originalDescription;
    private Map<TransactionPayloadTypes, String> payload;

    @JsonIgnore
    private String payloadSerialized;

    private boolean pending;
    private long timestamp;
    private TransactionTypes type;
    private String userId;
    private boolean upcoming;
    private boolean userModifiedAmount;
    private boolean userModifiedCategory;
    private boolean userModifiedDate;
    private boolean userModifiedDescription;
    private boolean userModifiedLocation;
    private List<TransactionPart> parts;
    private BigDecimal dispensableAmount;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final TypeReference<HashMap<String, String>> INTERNAL_PAYLOAD_TYPE_REFERENCE = new TypeReference<HashMap<String, String>>() {

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

    public double getAmount() {
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

    public Map<String, String> getInternalPayload() {
        if (internalPayload == null) {
            internalPayload = new HashMap<String, String>();
        }

        return internalPayload;
    }

    public String getInternalPayload(String key) {
        return getInternalPayload().get(key);
    }

    @JsonIgnore
    public String getInternalPayloadSerialized() {
        try {
            return OBJECT_MAPPER.writeValueAsString(getInternalPayload());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
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

    public void setAmount(double amount) {
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

    public void copyModifiableFields(Transaction transaction) {
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

    public void setDispensableAmount(BigDecimal dispensableAmount) {
        this.dispensableAmount = dispensableAmount;
    }
}
