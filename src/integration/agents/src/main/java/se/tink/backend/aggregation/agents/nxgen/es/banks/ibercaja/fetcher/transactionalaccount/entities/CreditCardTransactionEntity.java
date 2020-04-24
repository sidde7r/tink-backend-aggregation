package se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class CreditCardTransactionEntity {

    @JsonProperty("FechaOperacion")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date dateOfTransaction;

    @JsonProperty("Concepto")
    private String description;

    @JsonProperty("Importe")
    private double amount;

    @JsonIgnore
    public CreditCardTransaction toTinkTransaction() {
        return CreditCardTransaction.builder()
                .setAmount(ExactCurrencyAmount.inEUR(amount))
                .setDate(dateOfTransaction)
                .setDescription(description)
                .build();
    }
}
