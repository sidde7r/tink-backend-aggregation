package se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.transactional.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionEntity {
  @JsonProperty("record")
  private TransactionRecord transactionRecord;

  public Transaction toTinkTransaction() {
    return transactionRecord.toTinkTransaction();
  }

  public boolean isValid() {
    return transactionRecord.isValid();
  }
}
