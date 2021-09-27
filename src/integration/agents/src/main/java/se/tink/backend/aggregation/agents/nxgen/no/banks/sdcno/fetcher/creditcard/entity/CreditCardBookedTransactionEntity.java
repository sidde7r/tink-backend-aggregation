package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.fetcher.creditcard.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import se.tink.backend.aggregation.agents.models.TransactionTypes;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Getter
@JsonObject
@AllArgsConstructor
@NoArgsConstructor
public class CreditCardBookedTransactionEntity implements CreditCardTransactionEntity {
    @JsonProperty("posteringTekst")
    private String description;

    @JsonProperty("transaksjonsDato")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date transactionDate;

    @JsonProperty("registreringsDato")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date bookedTransactionDate;

    @JsonProperty("overfortBelop")
    private String transferredAmount;

    @JsonProperty("originalBelop")
    private String originalAmount;

    @JsonProperty("overfortValutaIsoKode")
    private String currency;

    public AggregationTransaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(
                        ExactCurrencyAmount.of(
                                ObjectUtils.firstNonNull(transferredAmount, originalAmount),
                                currency))
                .setDate(ObjectUtils.firstNonNull(transactionDate, bookedTransactionDate))
                .setDescription(description)
                .setPending(false)
                .setType(TransactionTypes.CREDIT_CARD)
                .build();
    }
}
