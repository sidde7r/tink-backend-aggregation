package se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditDataEntity {
  @JsonProperty("Transaction")
  private List<TransactionEntity> transactions;

  public List<TransactionEntity> getTransactions() {
    return transactions;
  }
}
