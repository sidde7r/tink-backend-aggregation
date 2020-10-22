package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.checking.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.models.TransactionPayloadTypes;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.utils.json.deserializers.LocalDateDMYFormatDeserializer;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionEntity {
    @JsonProperty("dataContabile")
    @JsonDeserialize(using = LocalDateDMYFormatDeserializer.class)
    private LocalDate bookedDate;

    @JsonProperty("dataValuta")
    @JsonDeserialize(using = LocalDateDMYFormatDeserializer.class)
    private LocalDate valueDate;

    @JsonProperty("descrizioneCausale")
    private String shortDescription;

    @JsonProperty("header2")
    private String longDescription;

    @JsonProperty("importo")
    private String amount;

    @JsonProperty("segno")
    private String amountSymbol;

    public Transaction toTinkTransaction(String currency) {
        boolean isPending = this.bookedDate == null;
        LocalDate date = isPending ? this.valueDate : this.bookedDate;

        BigDecimal calculatedAmount =
                new BigDecimal(this.amountSymbol + this.amount).movePointLeft(2);

        return Transaction.builder()
                .setDate(date)
                .setAmount(ExactCurrencyAmount.of(calculatedAmount, currency))
                .setDescription(shortDescription)
                .setPayload(TransactionPayloadTypes.DETAILS, longDescription)
                .setPending(isPending)
                .build();
    }
}
