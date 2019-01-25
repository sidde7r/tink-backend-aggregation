package se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.fetcher.creditcards.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.libraries.amount.Amount;

@JsonObject
public class CreditCardTransactionEntity {
    private String transaktionsId;
    @JsonProperty("text")
    private String description;
    @JsonProperty("belopp")
    private double amount;
    @JsonProperty("datum")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date date;
    private boolean harDetaljer;
    private String tankningsId;
    @JsonProperty("ejBokford")
    private boolean pending;
    private boolean beloppInkuderarAndraKategorier;

    public CreditCardTransaction toTinkTransaction() {
        return CreditCardTransaction.builder()
                .setAmount(Amount.inSEK(amount))
                .setDate(date)
                .setDescription(description)
                .setPending(pending)
                .build();
    }
}
