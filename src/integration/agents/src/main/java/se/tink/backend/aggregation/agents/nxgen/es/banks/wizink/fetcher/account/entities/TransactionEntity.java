package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.account.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class TransactionEntity {
    @JsonProperty("concept")
    private String description;

    @JsonFormat(pattern = "dd/MM/yyyy")
    private Date operationDate;

    private String amount;

    private String parseAmount() {
        return amount.replace(",", ".");
    }

    @JsonIgnore
    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setDescription(description)
                .setDate(operationDate)
                .setAmount(ExactCurrencyAmount.of(parseAmount(), "EUR"))
                .build();
    }
}
