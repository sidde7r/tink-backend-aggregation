package se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.fetcher.transactions.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.core.Amount;

@JsonObject
public class TransactionEntity {

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

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(Amount.inSEK(amount))
                .setDate(date)
                .setDescription(description)
                .setPending(pending)
                .build();
    }
}
