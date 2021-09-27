package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.fetcher.creditcard.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.models.TransactionTypes;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreditCardPendingTransactionEntity implements CreditCardTransactionEntity {
    @JsonProperty("beskrivelse")
    private String description;

    @JsonProperty("registreringsDato")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date transactionDate;

    @JsonProperty("belop")
    private String amount;

    @JsonProperty("valutaIsoKode")
    private String currency;

    public AggregationTransaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(ExactCurrencyAmount.of(amount, currency))
                .setDate(transactionDate)
                .setDescription(description)
                .setPending(true)
                .setType(TransactionTypes.CREDIT_CARD)
                .build();
    }
}
