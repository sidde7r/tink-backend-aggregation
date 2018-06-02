package se.tink.backend.aggregation.agents.banks.nordea.v15.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountTransactionsEntity {

    private final static TypeReference<List<TransactionEntity>> LIST_TYPE_REFERENCE = new TypeReference<List<TransactionEntity>>() {
    };
    private final static ObjectMapper MAPPER = new ObjectMapper();

    private Map<String, Object> accountId = new HashMap<String, Object>();
    private Map<String, Object> continueKey = new HashMap<String, Object>();

    @JsonProperty("accountTransaction")
    private List<TransactionEntity> accountTransactions;

    public Map<String, Object> getContinueKey() {
        return continueKey;
    }

    public void setContinueKey(Map<String, Object> continueKey) {
        this.continueKey = continueKey;
    }

    public Map<String, Object> getAccountId() {
        return accountId;
    }

    public void setAccountId(Map<String, Object> accountId) {
        this.accountId = accountId;
    }

    public List<TransactionEntity> getAccountTransactions() {
        return accountTransactions;
    }

    /**
     * Nordea API is a bit weird and send items on different formats depending on the number of items. Multiple
     * rows means that we will get an List of items and one row will not be typed as an array.
     */
    public void setAccountTransactions(Object input) {

        if (input instanceof Map) {
            accountTransactions = Lists.newArrayList(MAPPER.convertValue(input, TransactionEntity.class));
        } else {
            accountTransactions = MAPPER.convertValue(input, LIST_TYPE_REFERENCE);
        }
    }

}
