package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class CardTransactionEntity {

    @JsonProperty("movementdescription")
    private String movementDescription;

    @JsonProperty("movementamount")
    private String movementAmount;

    private String currency;

    @JsonFormat(pattern = "dd/MM/yyyy")
    private Date dateToOrder;

    public CreditCardTransaction toTinkTransaction() {
        return CreditCardTransaction.builder()
                .setAmount(ExactCurrencyAmount.of(movementAmount, getCurrency()))
                .setDate(dateToOrder)
                .setDescription(movementDescription)
                .setRawDetails(this)
                .build();
    }

    private String getCurrency() {
        return Strings.isNullOrEmpty(currency) ? "EUR" : currency;
    }
}
