package se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class BookedEntity {
  private String transactionId;

  private String valueDate;

  @JsonFormat(pattern = "yyyy-MM-dd")
  private Date bookingDate;

  private String entryReference;

  private String descriptiveText;

  private TransactionAmountEntity transactionAmount;

  private String proprietaryBankTransactionCode;

  private String proprietaryBankTransactionCodeText;

  public Transaction toTinkTransaction() {
    return Transaction.builder()
        .setAmount(transactionAmount.getAmount())
        .setDate(bookingDate)
        .setDescription(descriptiveText)
        .setPending(false)
        .build();
  }
}
