package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.date.DateUtils;

@JsonObject
public class TransactionsBodyEntity {
    @JsonProperty("Transactions")
    private List<TransactionEntity> transactions;

    @JsonProperty("NoMoreTransactions")
    private boolean noMoreTransactions;

    @JsonProperty("FromDate")
    private String fromDate;

    @JsonProperty("ToDate")
    private String toDate;

    @JsonIgnore
    public Date getNextKey() {
        return noMoreTransactions ? null : getNextDate();
    }

    /**
     * The next date will be the posted date of the oldest transaction minus one day. This is how
     * the ICA Banken app does the transaction fetching.
     */
    private Date getNextDate() {
        return transactions.stream()
                .min(Comparator.comparing(TransactionEntity::getPostedDate))
                .map(transactionEntity -> DateUtils.addDays(transactionEntity.getPostedDate(), -1))
                .orElse(null);
    }

    public List<TransactionEntity> getTransactions() {
        return transactions;
    }

    public boolean isNoMoreTransactions() {
        return noMoreTransactions;
    }

    public String getFromDate() {
        return fromDate;
    }

    public String getToDate() {
        return toDate;
    }
}
