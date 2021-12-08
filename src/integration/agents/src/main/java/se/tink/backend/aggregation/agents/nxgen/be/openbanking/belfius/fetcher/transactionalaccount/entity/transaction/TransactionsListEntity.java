package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.fetcher.transactionalaccount.entity.transaction;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.apache.commons.collections4.ListUtils;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionsListEntity {

    @JsonProperty("next_page_key")
    private String nextPageKey;

    @JsonProperty("booked")
    private List<BookedTransactionEntity> bookedTransactionEntities;

    public String getNextPageKey() {
        return nextPageKey;
    }

    public List<BookedTransactionEntity> getTransactions() {
        return ListUtils.emptyIfNull(bookedTransactionEntities);
    }
}
