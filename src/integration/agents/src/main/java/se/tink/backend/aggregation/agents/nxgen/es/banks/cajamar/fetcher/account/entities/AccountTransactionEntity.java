package se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.account.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Getter
@JsonObject
public class AccountTransactionEntity {
    private String documentId;
    private String description;
    private String date;
    private String currency;
    private BigDecimal amount;
    private BigDecimal balance;

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(ExactCurrencyAmount.of(amount, currency))
                .setDescription(description)
                .setDate(LocalDate.parse(date))
                .build();
    }
}
