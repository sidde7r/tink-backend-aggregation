package se.tink.backend.aggregation.agents.banks.nordea.v15.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreditCardTransactionsEntity {

    private static final TypeReference<List<TransactionEntity>> LIST_TYPE_REFERENCE =
            new TypeReference<List<TransactionEntity>>() {};
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private List<TransactionEntity> transactions;

    public List<TransactionEntity> getTransactions() {
        return transactions;
    }

    /**
     * Nordea API is a bit weird and send items on different formats depending on the number of
     * items. Multiple rows means that we will get an List of items and one row will not be typed as
     * an array.
     */
    public void setTransactions(Object input) {

        if (input instanceof Map) {
            transactions = Lists.newArrayList(MAPPER.convertValue(input, TransactionEntity.class));
        } else {
            transactions = MAPPER.convertValue(input, LIST_TYPE_REFERENCE);
        }
    }
}
