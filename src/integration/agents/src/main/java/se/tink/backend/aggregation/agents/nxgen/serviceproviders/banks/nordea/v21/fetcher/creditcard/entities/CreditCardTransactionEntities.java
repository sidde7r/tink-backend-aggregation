package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.creditcard.entities;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.serializer.NordeaHashMapDeserializer;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditCardTransactionEntities {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private List<CreditCardTransactionEntity> transactions;
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String continuationKey;

    public List<CreditCardTransactionEntity> getTransactions() {
        return transactions != null ? transactions : Collections.emptyList();
    }

    public String getContinuationKey() {
        return continuationKey;
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
            transactions = Lists.newArrayList(MAPPER.convertValue(input, CreditCardTransactionEntity.class));
        } else {
            transactions = MAPPER.convertValue(input, new TypeReference<List<CreditCardTransactionEntity>>() {});
        }
    }
}
