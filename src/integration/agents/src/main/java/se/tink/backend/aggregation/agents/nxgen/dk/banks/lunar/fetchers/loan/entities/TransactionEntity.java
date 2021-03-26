package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.loan.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class TransactionEntity {
    private BigDecimal amount;
    private String currency;
    @Getter private String displayDate;
    private String title;

    @JsonIgnore
    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(ExactCurrencyAmount.of(amount, currency))
                .setDescription(title)
                .setDate(Date.from(Instant.parse(displayDate)))
                .build();
    }
}
