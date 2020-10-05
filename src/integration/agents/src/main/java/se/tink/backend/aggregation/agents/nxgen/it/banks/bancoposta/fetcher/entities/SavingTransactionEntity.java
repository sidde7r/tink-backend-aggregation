package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@Getter
public class SavingTransactionEntity {
    @JsonProperty("dataContabile")
    @JsonFormat(pattern = "dd/MM/yyyy")
    private Date bookedDate;

    @JsonProperty("dataValuta")
    @JsonFormat(pattern = "dd/MM/yyyy")
    private Date valueDate;

    @JsonProperty("descrizione")
    private String description;

    @JsonProperty("importo")
    private String amount;

    @JsonProperty("divisa")
    private String currencyCode;

    public Transaction toTinkTransaction() {
        boolean isPending = this.bookedDate == null;
        Date date = isPending ? this.bookedDate : this.valueDate;

        BigDecimal calculatedAmount = new BigDecimal(this.amount).movePointLeft(2);

        return Transaction.builder()
                .setDate(date)
                .setAmount(ExactCurrencyAmount.of(calculatedAmount, this.currencyCode))
                .setDescription(description)
                .setPending(isPending)
                .build();
    }
}
