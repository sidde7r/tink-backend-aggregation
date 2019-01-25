package se.tink.backend.aggregation.agents.banks.uk.barclays.entities.transaction;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.text.ParseException;
import java.util.Date;
import se.tink.backend.system.rpc.Transaction;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.strings.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionEntity {
    public static final ThreadSafeDateFormat DATE_FORMATTER = new ThreadSafeDateFormat(  "dd/MM/yyyy");

    //"nudgedText": null,
    //"nudgedRetailerId": null,
    private boolean nudged;
    @JsonProperty("narrative2")
    private String reference;
    private String typeCode;
    private String amount;
    private boolean regular;
    private String narrative3to15;
    private String preciseTime;
    private String date;
    private String secCode;
    private String type;
    @JsonProperty("narrative1")
    private String description;
    private String runningBalance;

    public boolean isNudged() {
        return nudged;
    }

    public String getReference() {
        return reference;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public String getAmount() {
        return amount;
    }

    public boolean isRegular() {
        return regular;
    }

    public String getNarrative3to15() {
        return narrative3to15;
    }

    public String getPreciseTime() {
        return preciseTime;
    }

    public Date getDate() {
        try {
            return DATE_FORMATTER.parse(date);
        } catch (ParseException e) {
            throw new IllegalStateException(e);
        }
    }

    public String getSecCode() {
        return secCode;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public String getRunningBalance() {
        return runningBalance;
    }

    public Transaction toTinkTransaction() {
        Transaction tinkTransaction = new Transaction();
        tinkTransaction.setAmount(StringUtils.parseAmount(getAmount()));
        tinkTransaction.setDate(getDate());
        tinkTransaction.setDescription(description);
        return tinkTransaction;
    }
}
