package se.tink.backend.aggregation.agents.nxgen.fr.banks.labanquepostale.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.labanquepostale.LaBanquePostaleConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;

@JsonObject
public class TransactionEntity {

    private LocalDate date;

    @JsonProperty("montant")
    private Amount amount;

    @JsonProperty("libelle")
    private String label;

    public void setAmount(double amount) {
        this.amount = new Amount(LaBanquePostaleConstants.CURRENCY, amount);
    }

    public void setDate(String date) {
        this.date = LocalDate.parse(date, LaBanquePostaleConstants.DATE_FORMATTER);
    }

    public Transaction toTinkTransaction() {

        Transaction.Builder builder = Transaction.builder();

        builder.setDate(date);
        builder.setAmount(amount);
        builder.setDescription(label);

        return builder.build();
    }
}
