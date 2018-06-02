package se.tink.backend.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import io.protostuff.Exclude;
import io.protostuff.Tag;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import se.tink.backend.utils.StringUtils;
import se.tink.libraries.serialization.utils.SerializationUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Activity {

    public static final class Types
    {
        public static final String DOUBLE_CHARGE = "double-charge";
        public static final String FOLLOW = "follow";
        public static final String FOLLOW_EXPENSES = FOLLOW + "/expenses";
        public static final String FOLLOW_SEARCH = FOLLOW + "/search";
        public static final String LARGE_EXPENSE = "large-expense";
        public static final String LARGE_EXPENSE_MULTIPLE = LARGE_EXPENSE + "/multiple";
        public static final String TRANSACTION = "transaction";
        public static final String TRANSACTION_MULTIPLE = TRANSACTION + "/multiple";
        public static final String INCOME = "income";
        public static final String INCOME_MULTIPLE = INCOME + "/multiple";
        public static final String BALANCE = "balance";
        public static final String BALANCE_LOW = BALANCE + "-low";
        public static final String BALANCE_HIGH = BALANCE + "-high";
        public static final String TRANSFER = "transfer";
        public static final String UNUSUAL_ACCOUNT = "unusual-account";
        public static final String UNUSUAL_CATEGORY = "unusual-category";
        public static final String UNUSUAL_CATEGORY_HIGH = UNUSUAL_CATEGORY + "-high";
        public static final String UNUSUAL_CATEGORY_LOW = UNUSUAL_CATEGORY + "-low";
        public static final String MONTHLY_SUMMARY = "monthly-summary";
        public static final String MONTHLY_SUMMARY_ABNAMRO = MONTHLY_SUMMARY + "/abnamro";
        public static final String WEEKLY_SUMMARY = "weekly-summary";
        public static final String SUGGEST = "suggest";
        public static final String LEFT_TO_SPEND = "left-to-spend";
        public static final String NET_INCOME = "net-income";
        public static final String CUSTOM_TEXT = "custom-text";
        public static final String SUGGEST_MERCHANTS = "suggest-merchants";
        public static final String BADGE = "badge";
        public static final String LOOKBACK = "lookback";
        public static final String MERCHANT_HEAT_MAP = "merchant-heat-map";
        public static final String FRAUD = "fraud";
        public static final String SUGGEST_PROVIDER = "suggest-provider";
        public static final String BANK_FEE = "bank-fee";
        public static final String BANK_FEE_MULTIPLE = BANK_FEE + "/multiple";
        public static final String BANK_SELFIE = BANK_FEE + "-selfie";
        public static final String DISCOVER = "discover";
        public static final String DISCOVER_BUDGETS = DISCOVER + "/budgets";
        public static final String DISCOVER_CATEGORIES = "discover/categories";
        public static final String DISCOVER_EMPTY = DISCOVER + "/empty";
        public static final String EINVOICES = "e-invoices";
        public static final String APPLICATION_MORTGAGE = "application/mortgage";
        public static final String APPLICATION_SAVINGS = "application/savings";
        public static final String AUTOMATIC_SAVINGS_SUMMARY_ABNAMRO = "automatic-savings-summary/abnamro";
        public static final String MAINTENANCE_INFORMATION_ABNAMRO = "maintenance-information/abnamro";
        public static final String YEAR_IN_NUMBERS = "year-in-numbers";
        public static final String SUMMER_IN_NUMBERS = "summer-in-numbers";
        public static final String APPLICATION_RESUME_MORTGAGE = "application-resume/mortgage";
        public static final String APPLICATION_RESUME_SAVINGS = "application-resume/savings";
        public static final String RATE_THIS_APP = "rate-this-app";
        public static final String REIMBURSEMENT = "reimbursement";
        public static final String LOAN = "loan";
        public static final String LOAN_DECREASE = LOAN + "-decrease";
        public static final String LOAN_INCREASE = LOAN + "-increase";
    }

    protected static final ObjectMapper mapper = new ObjectMapper();

    @Exclude
    @ApiModelProperty(name = "content", value="Serialized type dependent object.", required = true, example="{\"categoryId\": \"18bb1f4636894f3bba8ddcd567d22fbd\", \"period\": \"2016-05\", \"data\": [\"2015-01\"...")
    protected Object content;
    // @Tag(1)
    // Removed `contentSerialized` since it's redundant.
    @Tag(2)
    @ApiModelProperty(name = "date", value="Date of the activity.", required = true, example = "1455740874875")
    protected Date date;
    @JsonIgnore
    @Exclude
    @ApiModelProperty(name = "generator", hidden = true)
    protected String generator;
    @Tag(3)
    @ApiModelProperty(name = "id", hidden = true)
    protected String id;
    @Tag(4)
    @ApiModelProperty(name = "importance", value="Importance compared to other activities 1-100 where 100 is of most importance.", example="62.5", required = true)
    protected double importance;
    @Exclude
    @ApiModelProperty(name = "inserted", hidden = true)
    protected long inserted;
    @Tag(5)    
    @ApiModelProperty(name = "key", value="Persistent key per type and content.", example="unusual-category-high.2016-05.18bb1f4636894f3bba8ddcd567d22fbd", required = true)
    protected String key;
    @Tag(6)
    @ApiModelProperty(name = "message", value="The activity message. Used as basis for the corresponding notification.", example="You have spent more than usual on restaurants this month.", required = true)
    protected String message;
    @Exclude
    @ApiModelProperty(name = "timestamp", hidden = true)
    protected long timestamp;
    @Tag(7)
    @ApiModelProperty(name = "title", value="The activity title. Used as basis for the corresponding notification.", example="More than usual", required = true)
    protected String title;
    @Tag(8)
    @ApiModelProperty(name = "type", value="The activity type. Used as basis for the corresponding notification.", example="unusual-category-high", required = true)
    protected String type;
    @Tag(9)
    @ApiModelProperty(name = "userId", hidden = true)
    protected String userId;
    @Exclude
    @ApiModelProperty(name = "minIosVersion", hidden = true)
    protected String minIosVersion;
    @Exclude
    @ApiModelProperty(name = "minAndroidVersion", hidden = true)
    protected String minAndroidVersion;
    @Exclude
    @ApiModelProperty(name = "maxIosVersion", hidden = true)
    protected String maxIosVersion;
    @Exclude
    @ApiModelProperty(name = "maxAndroidVersion", hidden = true)
    protected String maxAndroidVersion;
    @Exclude
    @ApiModelProperty(name = "feedActivityIdentifier", hidden = true)
    protected String feedActivityIdentifier;
    @Exclude
    @ApiModelProperty(name = "sensitiveMessage", hidden = true)
    protected String sensitiveMessage;

    public Activity() {
        id = StringUtils.generateUUID();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof Activity) {
            final Activity other = (Activity) obj;

            return (Objects.equal(id, other.id));
        } else {
            return false;
        }
    }

    public Object getContent() {
        return content;
    }

    public <T> T getContent(Class<T> cls) {
        // When `content` is deserialized to an `Object`, complex types become a `LinkedHashMap` which is not directly
        // castable into the complex type we want. So we need to serialize it and then deserialize it into an explicitly
        // specified class.
        return SerializationUtils.deserializeFromBinary(SerializationUtils.serializeToBinary(getContent()), cls);
    }

    public <T> T getContent(TypeReference<T> typeReference) {
        // When `content` is deserialized to an `Object`, complex types become a `LinkedHashMap` which is not directly
        // castable into the complex type we want. So we need to serialize it and then deserialize it into an explicitly
        // specified class, based on the type reference.
        return SerializationUtils.deserializeFromBinary(SerializationUtils.serializeToBinary(getContent()),
                typeReference);
    }

    public Date getDate() {
        return date;
    }

    public String getGenerator() {
        return generator;
    }

    public String getId() {
        return id;
    }

    public double getImportance() {
        return importance;
    }

    public long getInserted() {
        return inserted;
    }

    public String getKey() {
        return key;
    }

    public String getMessage() {
        return message;
    }
    
    public long getTimestamp() {
        return timestamp;
    }

    public String getTitle() {
        return title;
    }

    public String getType() {
        return type;
    }

    public String getUserId() {
        return userId;
    }

    public String getMinIosVersion() {
        return minIosVersion;
    }

    public String getMinAndroidVersion() {
        return minAndroidVersion;
    }

    public String getMaxIosVersion() {
        return maxIosVersion;
    }

    public String getMaxAndroidVersion() {
        return maxAndroidVersion;
    }

    public String getFeedActivityIdentifier()
    {
        // Null check for legacy clients
        if (feedActivityIdentifier == null) {
            feedActivityIdentifier = "";
        }

        // Check for V2 clients
        return feedActivityIdentifier;
    }

    public String getSensitiveMessage() {
        return sensitiveMessage;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setGenerator(String generator) {
        this.generator = generator;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setImportance(double importance) {
        this.importance = importance;
    }

    public void setInserted(long inserted) {
        this.inserted = inserted;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setMinIosVersion(String minIosVersion) {
        this.minIosVersion = minIosVersion;
    }

    public void setMinAndroidVersion(String minAndroidVersion) {
        this.minAndroidVersion = minAndroidVersion;
    }

    public void setMaxIosVersion(String maxIosVersion) {
        this.maxIosVersion = maxIosVersion;
    }

    public void setMaxAndroidVersion(String maxAndroidVersion) {
        this.maxAndroidVersion = maxAndroidVersion;
    }

    public void setFeedActivityIdentifier(String feedActivityIdentifier) {
        this.feedActivityIdentifier = feedActivityIdentifier;
    }

    public void setSensitiveMessage(String sensitiveMessage) {
        this.sensitiveMessage = sensitiveMessage;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("userId", userId)
                .add("date", date).add("type", type).add("title", title)
                .add("message", message)
                .add("content", content).toString();
    }

}
