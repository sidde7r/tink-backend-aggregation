package se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.fetcher.entity;

import com.google.common.base.Strings;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.ErsteBankConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionEntity {
    private String id;
    private AmountEntity amount;
    private String title;
    private String subtitle;
    private String date;

    public String getId() {
        return id;
    }

    public AmountEntity getAmount() {
        return amount;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getDate() {
        return date;
    }

    public boolean isTransaction() {
        return amount != null;
    }

    public boolean isValidTransaction() {
        return !Strings.isNullOrEmpty(title) && !Strings.isNullOrEmpty(date) && amount != null;
    }

    private Date toTinkDate() {
        try {
            return new SimpleDateFormat(ErsteBankConstants.PATTERN.DATE_FORMAT).parse(getDate());
        } catch (ParseException e) {
            throw new IllegalArgumentException("Cannot parse date: " + e.toString());
        }
    }

    public Transaction toTinkTransaction() {

        return Transaction.builder()
                .setAmount(getAmount().getTinkBalance())
                .setDate(toTinkDate())
                .setDescription(getTitle())
                .setExternalId(id)
                .build();

    }
}
