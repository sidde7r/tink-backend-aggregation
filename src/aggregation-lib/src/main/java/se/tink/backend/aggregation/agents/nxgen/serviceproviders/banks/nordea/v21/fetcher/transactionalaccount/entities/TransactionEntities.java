package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.serializer.NordeaHashMapDeserializer;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionEntities {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @JsonProperty("accountTransaction")
    private List<TransactionEntity> transactions;
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String accountId;
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String continueKey;

    public List<TransactionEntity> getTransactions() {
        return transactions != null ? transactions : Collections.emptyList();
    }

    /**
     * Nordea API is a bit weird and send items on different formats depending on the number of items. Multiple
     * rows means that we will get an List of items and one row will not be typed as an array.
     */
    public void setTransactions(Object input) {
        if (input == null) {
            return;
        }

        if (input instanceof Map) {
            transactions = Lists.newArrayList(MAPPER.convertValue(input, TransactionEntity.class));
        } else {
            transactions = MAPPER.convertValue(input, new TypeReference<List<TransactionEntity>>() {});
        }
    }

    public String getContinueKey() {
        return continueKey;
    }

    public void setContinueKey(String continueKey) {
        this.continueKey = continueKey;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }
}
