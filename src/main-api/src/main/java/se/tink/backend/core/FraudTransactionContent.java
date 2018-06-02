package se.tink.backend.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Strings;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Transient;
import org.hibernate.annotations.Type;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class FraudTransactionContent extends FraudDetailsContent {
    private static final TypeReference<List<FraudTransactionEntity>> STRING_LIST_TYPE_REFERENCE = new TypeReference<List<FraudTransactionEntity>>() {
    };

    // Having duplicate info for ID, but keeping transactionIds for legacy reasons.
    private List<String> transactionIds;
    private List<FraudTransactionEntity> transactions;
    private String transactionsSerialized;
    private String payload;

    public List<String> getTransactionIds() {
        return transactionIds;
    }

    public void setTransactionIds(List<String> transactionIds) {
        this.transactionIds = transactionIds;
    }

    @Override
    public String generateContentId() {
        if (transactionIds == null) {
            return "null";
        }
        
        if (transactions != null && transactions.size() != 0) {

            Date date = transactions.get(0).getDate();

            return String.valueOf(Objects.hash(itemType(), contentType, date));

        } else {
            Collections.sort(transactionIds);
            return String.valueOf(Objects.hash(itemType(), transactionIds));
        }
    }

    @Transient
    public List<FraudTransactionEntity> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<FraudTransactionEntity> transactions) {
        if (transactions == null) {
            return;
        }

        this.transactions = transactions;

        if (transactions != null) {
            transactionsSerialized = SerializationUtils.serializeToString(transactions);
        }
    }

    @JsonIgnore
    @Column(name = "`transactions`")
    @Type(type = "text")
    public String getTransactionsSerialized() {
        if (transactions != null) {
            return SerializationUtils.serializeToString(transactions);
        } else {
            return transactionsSerialized;
        }
    }

    @JsonIgnore
    public void setTransactionsSerialized(String transactionsSerialized) {
        if (Strings.isNullOrEmpty(transactionsSerialized)) {
            return;
        }

        this.transactionsSerialized = transactionsSerialized;

        if (!Strings.isNullOrEmpty(transactionsSerialized)) {
            transactions = SerializationUtils.deserializeFromString(transactionsSerialized, STRING_LIST_TYPE_REFERENCE);
        }
    }

    @Override
    public FraudTypes itemType() {
        return FraudTypes.TRANSACTION;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}
