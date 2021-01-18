package se.tink.backend.aggregation.agents.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.MoreObjects;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.uuid.UUIDUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Transaction implements Comparable<Transaction>, Cloneable {
    // TODO: Find out if we can add ExactCurrencyAmount in this model

    private String accountId;
    private double amount;
    private String credentialsId;
    private Date date;
    private String description;
    private String id;
    private Map<String, String> internalPayload;
    private double originalAmount;
    private Map<TransactionPayloadTypes, String> payload;

    @JsonIgnore private String payloadSerialized;

    private boolean pending;
    private long timestamp;
    private TransactionTypes type;
    private String userId;
    private boolean upcoming;
    private Map<TransactionExternalSystemIdType, String> externalSystemIds;
    private TransactionMutability mutability;
    private ExactCurrencyAmount transactionAmount;
    private List<TransactionDate> transactionDates;
    private String proprietaryFinancialInstitutionType;
    private String merchantName;
    private String merchantCategoryCode;
    private String transactionReference;
    private String providerMarket;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private void generateIdIfMissing() {
        if (id == null) {
            id = UUIDUtils.generateUUID();
        }
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

    public String getCredentialsId() {
        return this.credentialsId;
    }

    public Date getDate() {
        return this.date;
    }

    public String getDescription() {
        return this.description;
    }

    public String getId() {
        generateIdIfMissing();
        return id;
    }

    public Map<String, String> getInternalPayload() {
        if (internalPayload == null) {
            internalPayload = new HashMap<String, String>();
        }

        return internalPayload;
    }

    public double getOriginalAmount() {
        return originalAmount;
    }

    public Map<TransactionPayloadTypes, String> getPayload() {
        if (payload == null) {
            payload = new HashMap<TransactionPayloadTypes, String>();
        }

        return payload;
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

    public boolean isPending() {
        return pending;
    }

    public boolean isUpcoming() {
        return upcoming;
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

    public void setCredentialsId(String credentials) {
        this.credentialsId = credentials;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setInternalPayload(String key, String value) {
        getInternalPayload().put(key, value);
    }

    public void setOriginalAmount(double originalAmount) {
        this.originalAmount = originalAmount;
    }

    public void setPayload(TransactionPayloadTypes key, String value) {
        getPayload().put(key, value);
        serializePayload();
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

    public void setUpcoming(boolean upcoming) {
        this.upcoming = upcoming;
    }

    public Map<TransactionExternalSystemIdType, String> getExternalSystemIds() {
        return externalSystemIds;
    }

    public void setExternalSystemIds(
            Map<TransactionExternalSystemIdType, String> externalSystemIds) {
        this.externalSystemIds = externalSystemIds;
    }

    public TransactionMutability getMutability() {
        return mutability;
    }

    public void setMutability(TransactionMutability mutability) {
        this.mutability = mutability;
    }

    public ExactCurrencyAmount getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(ExactCurrencyAmount transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public List<TransactionDate> getTransactionDates() {
        return transactionDates;
    }

    public void setTransactionDates(List<TransactionDate> transactionDates) {
        this.transactionDates = transactionDates;
    }

    public String getProprietaryFinancialInstitutionType() {
        return proprietaryFinancialInstitutionType;
    }

    public void setProprietaryFinancialInstitutionType(String proprietaryFinancialInstitutionType) {
        this.proprietaryFinancialInstitutionType = proprietaryFinancialInstitutionType;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getMerchantCategoryCode() {
        return merchantCategoryCode;
    }

    public void setMerchantCategoryCode(String merchantCategoryCode) {
        this.merchantCategoryCode = merchantCategoryCode;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public String getProviderMarket() {
        return providerMarket;
    }

    public void setProviderMarket(String providerMarket) {
        this.providerMarket = providerMarket;
    }

    public static boolean deepEquals(Transaction first, Transaction second) {
        if (first == null || second == null) {
            return false;
        }

        if (first == second) {
            return true;
        }

        return Double.compare(first.amount, second.amount) == 0
                && Double.compare(first.originalAmount, second.originalAmount) == 0
                && first.pending == second.pending
                && first.timestamp == second.timestamp
                && first.upcoming == second.upcoming
                && Objects.equals(first.credentialsId, second.credentialsId)
                && Objects.equals(first.date, second.date)
                && Objects.equals(first.description, second.description)
                && Objects.equals(first.payloadSerialized, second.payloadSerialized)
                && first.type == second.type
                && Objects.equals(first.userId, second.userId)
                && Objects.equals(first.externalSystemIds, second.externalSystemIds)
                && Objects.equals(first.mutability, second.mutability)
                && Objects.equals(first.transactionAmount, second.transactionAmount)
                && Objects.equals(first.transactionDates, second.transactionDates)
                && Objects.equals(
                        first.proprietaryFinancialInstitutionType,
                        second.proprietaryFinancialInstitutionType)
                && Objects.equals(first.merchantName, second.merchantName)
                && Objects.equals(first.merchantCategoryCode, second.merchantCategoryCode)
                && Objects.equals(first.transactionReference, second.transactionReference)
                && Objects.equals(first.providerMarket, second.providerMarket);
    }

    @Override
    public String toString() {
        generateIdIfMissing();
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("credentialsId", credentialsId)
                .add("accountId", accountId)
                .add("date", getDate())
                .add("payload", payload)
                .add("payloadSerialized", payloadSerialized)
                .add("pending", pending)
                .add("timestamp", timestamp)
                .add("type", type)
                .add("userId", userId)
                .add("upcoming", upcoming)
                .add("externalSystemIds", externalSystemIds)
                .add("mutability", mutability)
                .add("transactionAmount", transactionAmount)
                .add("proprietaryFinancialInstitutionType", proprietaryFinancialInstitutionType)
                .add("merchantName", merchantName)
                .add("merchantCategoryCode", merchantCategoryCode)
                .add("transactionReference", transactionReference)
                .add("providerMarket", providerMarket)
                .toString();
    }
}
