package se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.credit.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.text.ParseException;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.de.banks.santander.SantanderConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

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

  private String getDescription() {
    StringBuilder builder = new StringBuilder();

    if (!Strings.isNullOrEmpty(location)) {
      builder.append(location.trim());
    }

    if (!Strings.isNullOrEmpty(submitter)) {
      builder.append(" " + submitter.trim());
    }

    if (!Strings.isNullOrEmpty(transactionTypeDescription)) {
      builder.append(" " + transactionTypeDescription.trim());
    }
    return builder.toString().trim();
  }

  private Date getDate() {
    try {
      return SantanderConstants.DATE.DATE_FORMAT.parse(date);
    } catch (ParseException e) {
      logger.error(
          "{} cannot parse date: {}", SantanderConstants.LOGTAG.SANTANDER_DATE_PARSING_ERROR, date);
      throw new IllegalStateException("Cannot parse date!");
    }
  }

  public boolean isValid() {
    try {
      toTinkTransaction();
      return true;
    } catch (Exception e) {
      logger.error("{} {}", SantanderConstants.LOGTAG.SANTANDER_TRANSACTION_ERROR, e.toString());
      return false;
    }
  }

  private se.tink.libraries.amount.Amount getAmount() {
    return new se.tink.libraries.amount.Amount(amount.getdIVISA(), amount.getiMPORTE());
  }

  public Transaction toTinkTransaction() {
    return Transaction.builder()
        .setDescription(getDescription())
        .setDate(getDate())
        .setAmount(getAmount())
        .build();
  }
}
