package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionsDataEntity {
    @JsonProperty("numberTransactionsAccounting")
    private Long numberTransactionsAccounting;

    @JsonProperty("numberTransactionsNotAccounting")
    private Long numberTransactionsNotAccounting;

    @JsonProperty("nextAccounting")
    private Long nextAccounting;

    @JsonProperty("nextNotAccounting")
    private Long nextNotAccounting;

    @JsonProperty("updateDate")
    private String updateDate;

    @JsonProperty("updateHour")
    private String updateHour;

    @JsonProperty("totalOutput")
    private AmountEntity totalOutput;

    @JsonProperty("totalEnter")
    private AmountEntity totalEnter;

    @JsonProperty("transactionsAccounting")
    private List<TransactionEntity> transactionsAccounting;

    @JsonProperty("transactionsNotAccounting")
    private List<TransactionEntity> transactionsNotAccounting;

    public Long getNumberTransactionsAccounting() {
        return numberTransactionsAccounting;
    }

    public Long getNumberTransactionsNotAccounting() {
        return numberTransactionsNotAccounting;
    }

    public Long getNextAccounting() {
        return nextAccounting;
    }

    public Long getNextNotAccounting() {
        return nextNotAccounting;
    }

    public String getUpdateDate() {
        return updateDate;
    }

    public String getUpdateHour() {
        return updateHour;
    }

    public AmountEntity getTotalOutput() {
        return totalOutput;
    }

    public AmountEntity getTotalEnter() {
        return totalEnter;
    }

    public List<TransactionEntity> getTransactionsAccounting() {
        return transactionsAccounting;
    }

    public List<TransactionEntity> getTransactionsNotAccounting() {
        return transactionsNotAccounting;
    }
}
