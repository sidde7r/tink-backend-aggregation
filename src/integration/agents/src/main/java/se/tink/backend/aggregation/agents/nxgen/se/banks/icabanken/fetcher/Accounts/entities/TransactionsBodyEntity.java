package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import org.apache.commons.collections4.ListUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.utils.json.deserializers.LocalDateDeserializer;

@Getter
@JsonObject
public class TransactionsBodyEntity {
    @JsonProperty("Transactions")
    private List<TransactionEntity> transactions;

    @JsonProperty("NoMoreTransactions")
    private boolean noMoreTransactions;

    @JsonProperty("FromDate")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate fromDate;

    @JsonProperty("ToDate")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate toDate;

    @JsonIgnore
    public LocalDate getNextKey() {
        return noMoreTransactions ? null : getNextToDate();
    }

    /** The next toDate will be the fromDate - 1 day */
    @JsonIgnore
    private LocalDate getNextToDate() {
        return fromDate == null ? null : fromDate.minusDays(1);
    }

    @JsonIgnore
    public Collection<Transaction> toTinkTransactions() {
        return ListUtils.emptyIfNull(transactions).stream()
                .map(TransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }
}
