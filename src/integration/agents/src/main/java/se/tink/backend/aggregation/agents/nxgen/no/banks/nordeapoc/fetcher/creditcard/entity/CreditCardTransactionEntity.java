package se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.fetcher.creditcard.entity;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.utils.json.deserializers.LocalDateDeserializer;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
public class CreditCardTransactionEntity {

    private BigDecimal amount;
    private String currency;
    private boolean booked;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate bookingDate;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate transactionDate;

    private String title;
    private String transactionId;

    public ExactCurrencyAmount getAmount() {
        return ExactCurrencyAmount.of(amount, currency);
    }
}
