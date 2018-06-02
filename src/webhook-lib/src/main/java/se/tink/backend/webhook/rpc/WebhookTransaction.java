package se.tink.backend.webhook.rpc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import se.tink.backend.core.CategoryTypes;
import se.tink.backend.core.TransactionPayloadTypes;
import se.tink.backend.core.TransactionTypes;
import se.tink.backend.firehose.v1.models.Category;
import se.tink.backend.firehose.v1.models.Transaction;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.webhook.WebhookHandler;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class WebhookTransaction {
    private static final ImmutableMap<Transaction.Type, TransactionTypes> FIREHOSE_TO_CORE_TRANSACTION_TYPE_MAPPING =
            ImmutableMap.<Transaction.Type, TransactionTypes>builder()
                    .put(Transaction.Type.TYPE_DEFAULT, TransactionTypes.DEFAULT)
                    .put(Transaction.Type.TYPE_CREDIT_CARD, TransactionTypes.CREDIT_CARD)
                    .put(Transaction.Type.TYPE_TRANSFER, TransactionTypes.TRANSFER)
                    .put(Transaction.Type.TYPE_PAYMENT, TransactionTypes.PAYMENT)
                    .put(Transaction.Type.TYPE_WITHDRAWAL, TransactionTypes.WITHDRAWAL)
                    .build();

    private static final ImmutableMap<Category.Type, CategoryTypes> FIREHOSE_TO_CORE_CATEGORY_TYPE_MAPPING =
            ImmutableMap.<Category.Type, CategoryTypes>builder()
                    .put(Category.Type.TYPE_EXPENSES, CategoryTypes.EXPENSES)
                    .put(Category.Type.TYPE_INCOME, CategoryTypes.INCOME)
                    .put(Category.Type.TYPE_TRANSFERS, CategoryTypes.TRANSFERS)
                    .build();

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final LogUtils log = new LogUtils(WebhookHandler.class);


    private String accountId;
    private double amount;
    private double originalAmount;
    private String categoryId;
    private String categoryType;
    private String credentialsId;
    private long date;
    private long originalDate;
    private String description;
    private String originalDescription;
    private String id;
    private long lastModified;
    private String merchantId;
    private String notes;
    private boolean pending;
    private long timestamp;
    private String type;
    private String userId;
    private boolean upcoming;
    private String externalId;
    private String payload;

    public static WebhookTransaction fromFirehoseTransaction(Transaction transaction) {
        if (transaction == null) {
            return null;
        }

        CategoryTypes categoryType = FIREHOSE_TO_CORE_CATEGORY_TYPE_MAPPING.get(transaction.getCategoryType());
        String categoryTypeString = categoryType == null ? null : categoryType.name();

        TransactionTypes transactionType = FIREHOSE_TO_CORE_TRANSACTION_TYPE_MAPPING.get(transaction.getType());
        String transactionTypeString = transactionType == null ? null : transactionType.name();

        WebhookTransaction webhookTransaction = new WebhookTransaction();

        webhookTransaction.accountId = transaction.getAccountId();
        webhookTransaction.amount = transaction.getAmount();
        webhookTransaction.originalAmount = transaction.getOriginalAmount();
        webhookTransaction.categoryId = transaction.getCategoryId();
        webhookTransaction.categoryType = categoryTypeString;
        webhookTransaction.credentialsId = transaction.getCredentialsId();
        webhookTransaction.date = transaction.getDate();
        webhookTransaction.originalDate = transaction.getOriginalDate();
        webhookTransaction.description = transaction.getDescription();
        webhookTransaction.originalDescription = transaction.getOriginalDescription();
        webhookTransaction.id = transaction.getId();
        webhookTransaction.lastModified = transaction.getLastModified();
        webhookTransaction.merchantId = transaction.getMerchantId();
        webhookTransaction.notes = transaction.getNotes();
        webhookTransaction.pending = transaction.getPending();
        webhookTransaction.timestamp = transaction.getTimestamp();
        webhookTransaction.type = transactionTypeString;
        webhookTransaction.userId = transaction.getUserId();
        webhookTransaction.upcoming = transaction.getUpcoming();
        webhookTransaction.payload = transaction.getPayloadSerialized();
        try {
            if(transaction.getPayloadSerialized() != null) {
                Map<TransactionPayloadTypes, String> payload = OBJECT_MAPPER
                        .readValue(transaction.getPayloadSerialized(),
                                new TypeReference<HashMap<TransactionPayloadTypes, String>>() {
                                });
                if (payload != null && payload.containsKey(TransactionPayloadTypes.EXTERNAL_ID)) {
                    webhookTransaction.externalId = payload.get(TransactionPayloadTypes.EXTERNAL_ID);
                }
            }
        } catch (IOException e) {
            log.error(transaction.getUserId(), String.format("Failed to parse payload: %s", transaction.getPayloadSerialized()), e);
        }
        return webhookTransaction;
    }

    public String getAccountId() {
        return accountId;
    }

    public double getAmount() {
        return amount;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getCategoryType() {
        return categoryType;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    public long getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public String getId() {
        return id;
    }

    public long getLastModified() {
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

    public long getOriginalDate() {
        return originalDate;
    }

    public String getOriginalDescription() {
        return originalDescription;
    }

    public boolean isPending() {
        return pending;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getType() {
        return type;
    }

    public String getUserId() {
        return userId;
    }

    public boolean isUpcoming() {
        return upcoming;
    }

    public String getPayload() {
        return payload;
    }

    public String getExternalId() {
        return externalId;
    }
}
