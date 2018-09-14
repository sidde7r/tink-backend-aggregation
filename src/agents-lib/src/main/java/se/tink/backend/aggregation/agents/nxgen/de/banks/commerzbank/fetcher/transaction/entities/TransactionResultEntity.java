package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.transaction.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionResultEntity {
    private Object metaData;
    @JsonProperty("items")
    private List<TransactionEntity> items;

    public List<TransactionEntity> getItems() {
        return items;
    }

    public boolean canFetchNextPage(int currentPage) {
        if (items == null || items.isEmpty()) {
            return false;
        }
        return items.get(0).canFetchNextPage(currentPage);
    }

    public void addAll(TransactionResultEntity transactionResultEntity) {
        if (items != null && !items.isEmpty()) {
            items.get(0).addAll(transactionResultEntity.getItems().get(0).getPfmTransactions());
        }
    }
}
