package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;

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

    private String type;

    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("accounting_date")
    private Date accountingDate;

    private String currency;

    @JsonIgnore
    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setDate(accountingDate)
                .setDescription(statement)
                .setPending(false)
                .setAmount(new Amount(currency, amount))
                .build();
    }
}
