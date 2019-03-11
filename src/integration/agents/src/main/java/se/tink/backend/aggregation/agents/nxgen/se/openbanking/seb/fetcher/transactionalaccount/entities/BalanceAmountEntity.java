package se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalanceAmountEntity {
  private String currency;

  private String amount;

  public String getCurrency() {
    return currency;
  }

  public double getAmount() {
    return Double.parseDouble(amount);
  }
}
