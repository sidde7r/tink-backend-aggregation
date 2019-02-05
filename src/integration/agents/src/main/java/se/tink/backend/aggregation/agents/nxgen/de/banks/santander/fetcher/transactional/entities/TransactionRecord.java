package se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.transactional.entities;

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

@JsonObject
public class TransactionRecord {
  @JsonProperty("TransactionDate")
  private String transactionDate;

  @JsonProperty("ApplicationDate")
  private String applicationDate;

  @JsonProperty("TransactionDesc01")
  private String description1;

  @JsonProperty("TransactionDesc02")
  private String description2;

  @JsonProperty("TransactionDesc03")
  private String description3;

  @JsonProperty("TransactionAmount")
  private TransactionAmount amount;

  @JsonIgnore private static final Logger logger = LoggerFactory.getLogger(TransactionRecord.class);

  private String getDescription() {
    StringBuilder builder = new StringBuilder();

    if (!Strings.isNullOrEmpty(description1)) {
      builder.append(description1.trim()).append(SantanderConstants.WHITESPACE);
    }

    if (!Strings.isNullOrEmpty(description2)) {
      builder.append(description2.trim()).append(SantanderConstants.WHITESPACE);
    }

    return builder.toString();
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

  private Date toDate() {
    try {
      return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").parse(transactionDate);
    } catch (ParseException e) {
      logger.error(
          "{} Cannot parse date: {}",
          SantanderConstants.LOGTAG.SANTANDER_DATE_PARSING_ERROR,
          transactionDate);
      throw new IllegalStateException("Cannot parse datetransaction");
    }
  }

  public Transaction toTinkTransaction() {
    return Transaction.builder()
        .setDescription(getDescription())
        .setDate(toDate())
        .setAmount(amount.toTinkAmount())
        .build();
  }
}
