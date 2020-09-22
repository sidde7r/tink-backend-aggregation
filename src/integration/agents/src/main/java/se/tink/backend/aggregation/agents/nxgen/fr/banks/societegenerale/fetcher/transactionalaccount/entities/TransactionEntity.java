package se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.models.TransactionTypes;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.SocieteGeneraleConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionEntity {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @JsonProperty("montant")
    private AmountEntity amount;

    @JsonProperty("dateJournal")
    private String date;

    @JsonProperty("libelle")
    private String label;

    private static Date toJavaLangDate(LocalDate localDate) {
        return new Date(
                localDate
                        .atStartOfDay(SocieteGeneraleConstants.ZONE_ID)
                        .toInstant()
                        .toEpochMilli());
    }

    private static Date toJavaLangDate(String dateAsString) {
        return toJavaLangDate(LocalDate.parse(dateAsString, DATE_FORMATTER));
    }

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setDate(toJavaLangDate(date))
                .setAmount(amount.toTinkAmount())
                .setDescription(label)
                .setType(getTransactionTypes())
                .build();
    }

    private TransactionTypes getTransactionTypes() {
        if (StringUtils.isBlank(label)) {
            return TransactionTypes.DEFAULT;
        }

        if (label.startsWith("VIR") || label.contains("VIR EUROPEEN")) {
            return TransactionTypes.TRANSFER;
        }

        if (label.contains("RETRAIT DAB")) {
            return TransactionTypes.WITHDRAWAL;
        }

        if (label.startsWith("CARTE")) {
            return TransactionTypes.CREDIT_CARD;
        }

        if (label.startsWith("COTISATION")) {
            return TransactionTypes.PAYMENT;
        }

        return TransactionTypes.DEFAULT;
    }
}
