package se.tink.backend.aggregation.agents.nxgen.fr.banks.labanquepostale.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.labanquepostale.LaBanquePostaleConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.core.Amount;

@JsonObject
public class TransactionEntity {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private LocalDate date;

    private Amount amount;
    @JsonProperty("libelle")
    private String label;

    private static Date toJavaLangDate(LocalDate localDate) {
        return new Date(
                localDate.atTime(LocalTime.NOON).atZone(LaBanquePostaleConstants.ZONE_ID).toInstant().toEpochMilli());
    }

    @JsonProperty("montant")
    public void setAmount(double amount) {
        this.amount = new Amount(LaBanquePostaleConstants.CURRENCY, amount);
    }

    public void setDate(String date) {
        this.date = LocalDate.parse(date, DATE_FORMATTER);
    }

    public Transaction toTinkTransaction() {

        Transaction.Builder builder = Transaction.builder();

        builder.setDate(toJavaLangDate(date));
        builder.setAmount(amount);
        builder.setDescription(label);

        return builder.build();
    }

}
