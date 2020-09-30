package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Getter;
import se.tink.backend.aggregation.agents.models.TransactionPayloadTypes;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@Getter
public class TransactionEntity {
    @JsonProperty("dataContabile")
    @JsonFormat(pattern = "dd/MM/yyyy")
    private Date bookedDate;

    @JsonProperty("dataValuta")
    @JsonFormat(pattern = "dd/MM/yyyy")
    private Date valueDate;

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
        Date date = isPending ? this.bookedDate : this.valueDate;

        BigDecimal calculatedAmount =
                new BigDecimal(this.amountSymbol + this.amount).divide(new BigDecimal("100"));

        return Transaction.builder()
                .setDate(date)
                .setAmount(ExactCurrencyAmount.of(calculatedAmount, currency))
                .setDescription(shortDescription)
                .setPayload(TransactionPayloadTypes.DETAILS, longDescription)
                .setPending(isPending)
                .build();
    }
}
