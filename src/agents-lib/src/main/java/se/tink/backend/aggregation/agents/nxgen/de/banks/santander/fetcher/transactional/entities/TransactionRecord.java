package se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.transactional.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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

  private Date toDate() {
    try {
      return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").parse(transactionDate);
    } catch (ParseException e) {
      e.printStackTrace();
    }
    return null;
  }

  public Transaction toTinkTransaction() {
    return Transaction.builder()
        .setDescription(getDescription())
        .setDate(toDate())
        .setAmount(amount.toTinkAmount())
        .build();
  }
}
