package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.fetcher.entities;

import java.math.BigDecimal;
import java.text.ParseException;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.date.ThreadSafeDateFormat;

@Data
@JsonObject
public class CreditCardTransactionEntity {
    private String description;
    private BigDecimal amount;
    private String currency;
    private String postingDate;

    public CreditCardTransaction toTinkTransaction() {
        try {
            return CreditCardTransaction.builder()
                    .setAmount(ExactCurrencyAmount.of(amount, currency))
                    .setDescription(description)
                    .setDate(ThreadSafeDateFormat.FORMATTER_DAILY.parse(postingDate))
                    .build();
        } catch (ParseException e) {
            throw new IllegalStateException("Parsing error with date");
        }
    }
}
