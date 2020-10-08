package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.utils.json.deserializers.LocalDateDMYFormatDeserializer;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@Getter
public class SavingTransactionEntity {
    @JsonProperty("dataContabile")
    @JsonDeserialize(using = LocalDateDMYFormatDeserializer.class)
    private LocalDate bookedDate;

    @JsonProperty("dataValuta")
    @JsonDeserialize(using = LocalDateDMYFormatDeserializer.class)
    private LocalDate valueDate;

    @JsonProperty("descrizione")
    private String description;

    @JsonProperty("importo")
    private String amount;

    @JsonProperty("divisa")
    private String currencyCode;

    public Transaction toTinkTransaction() {
        boolean isPending = this.bookedDate == null;
        LocalDate date = isPending ? this.valueDate : this.bookedDate;

        BigDecimal calculatedAmount = new BigDecimal(this.amount).movePointLeft(2);

        return Transaction.builder()
                .setDate(date)
                .setAmount(ExactCurrencyAmount.of(calculatedAmount, this.currencyCode))
                .setDescription(description)
                .setPending(isPending)
                .build();
    }
}
