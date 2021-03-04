package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class TransactionEntity {

    @JsonProperty("recurring_transfer")
    private Boolean recurringTransfer;

    @JsonProperty("transfer_id")
    private String transferId;

    @JsonProperty("value_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date valueDate;

    private Number amount;

    private String statement;

    @JsonProperty("counter_part_statement")
    private String counterPartStatement;

    private String type;

    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("accounting_date")
    private Date accountingDate;

    private String currency;

    @JsonIgnore
    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setDate(getTransactionDate())
                .setDescription(getStatement())
                .setPending(isPending())
                .setAmount(ExactCurrencyAmount.of(amount, currency))
                .build();
    }

    private Date getTransactionDate() {
        return Optional.ofNullable(accountingDate).orElse(valueDate);
    }

    private String getStatement() {
        return Optional.ofNullable(statement).orElse(counterPartStatement);
    }

    public Boolean isPending() {
        return "pending".equals(status);
    }
}
