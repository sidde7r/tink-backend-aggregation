package se.tink.backend.aggregation.agents.nxgen.fr.banks.labanquepostale.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;
import se.tink.backend.aggregation.agents.models.TransactionTypes;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.labanquepostale.LaBanquePostaleConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class TransactionEntity {

    private LocalDate date;

    @JsonProperty("montant")
    private BigDecimal amount;

    @JsonProperty("libelle")
    private String label;

    @JsonProperty("libelleComplementaire")
    private String additionalLabel;

    public void setDate(String date) {
        this.date = LocalDate.parse(date, LaBanquePostaleConstants.DATE_FORMATTER);
    }

    public Transaction toTinkTransaction() {

        Transaction.Builder builder = Transaction.builder();

        builder.setDate(date);
        builder.setAmount(ExactCurrencyAmount.of(amount, LaBanquePostaleConstants.CURRENCY));
        builder.setDescription(label + " " + additionalLabel);
        builder.setType(type());

        return builder.build();
    }

    private TransactionTypes type() {
        if (label.startsWith("ACHAT CB")) {
            return TransactionTypes.CREDIT_CARD;
        }
        if (label.startsWith("VIREMENT INSTANTANE")) {
            return TransactionTypes.TRANSFER;
        }
        if (label.contains("RETRAIT EFFECTUE")) {
            return TransactionTypes.WITHDRAWAL;
        }
        if (label.contains("VERSEMENT EFFECTUE")) {
            return TransactionTypes.PAYMENT;
        }
        return TransactionTypes.DEFAULT;
    }
}
