package se.tink.backend.aggregation.agents.nxgen.se.banks.seb.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class PendingTransactionEntity {
    @JsonProperty("ROW_ID")
    private Integer rowId;

    @JsonProperty("KONTO_NR")
    private BigInteger accountNumber;

    @JsonProperty("DATUM")
    private String date;

    @JsonProperty("KK_TXT")
    private String description;

    @JsonProperty("BELOPP")
    private BigDecimal amount;

    @JsonProperty("EXTERNAL_ID")
    private String externalId;

    @JsonIgnore
    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setDate(LocalDate.from(DateTimeFormatter.ISO_LOCAL_DATE.parse(date)))
                .setAmount(ExactCurrencyAmount.of(amount.negate(), "SEK"))
                .setDescription(description)
                .setPending(true)
                .build();
    }
}
