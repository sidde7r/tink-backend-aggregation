package se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.transactional.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.core.Amount;

@JsonObject
public class TransactionAmount {
  @JsonProperty("IMPORTE")
  private double amount;

  @JsonProperty("DIVISA")
  private String currency;

  public Amount toTinkAmount() {
    return new Amount(currency, amount);
  }
}
