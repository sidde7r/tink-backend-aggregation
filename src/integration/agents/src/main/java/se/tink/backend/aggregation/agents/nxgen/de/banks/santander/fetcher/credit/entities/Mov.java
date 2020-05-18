package se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.credit.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.de.banks.santander.SantanderConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class Mov {

    @JsonProperty("date")
    private String date;

    @JsonProperty("submitter")
    private String submitter;

    @JsonProperty("amount")
    private Amount amount;

    @JsonProperty("exchangeRate")
    private String exchangeRate;

    @JsonProperty("location")
    private String location;

    @JsonProperty("transctionCurrency")
    private String transctionCurrency;

    @JsonProperty("pan")
    private String pan;

    @JsonProperty("transactionTypeDescription")
    private String transactionTypeDescription;

    @JsonProperty("foreignAmount")
    private ForeignAmount foreignAmount;

    @JsonProperty("status")
    private String status;

    @JsonIgnore private static final Logger logger = LoggerFactory.getLogger(Mov.class);

    @JsonIgnore
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");

    private String getDescription() {
        StringBuilder builder = new StringBuilder();

        if (!Strings.isNullOrEmpty(location)) {
            builder.append(location.trim());
        }

        if (!Strings.isNullOrEmpty(submitter)) {
            builder.append(" ").append(submitter.trim());
        }

        if (!Strings.isNullOrEmpty(transactionTypeDescription)) {
            builder.append(" ").append(transactionTypeDescription.trim());
        }
        return builder.toString().trim();
    }

    private Date getDate() {
        try {
            return simpleDateFormat.parse(date);
        } catch (ParseException e) {
            logger.error(
                    "{} cannot parse date: {}",
                    SantanderConstants.LOGTAG.SANTANDER_DATE_PARSING_ERROR,
                    date,
                    e);
            throw new IllegalStateException("Cannot parse date!", e);
        }
    }

    public boolean isValid() {
        try {
            toTinkTransaction();
            return true;
        } catch (RuntimeException e) {
            logger.error(
                    "{} {}",
                    SantanderConstants.LOGTAG.SANTANDER_TRANSACTION_ERROR,
                    e.toString(),
                    e);
            return false;
        }
    }

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setDescription(getDescription())
                .setDate(getDate())
                .setAmount(ExactCurrencyAmount.of(amount.getiMPORTE(), amount.getdIVISA()))
                .build();
    }
}
