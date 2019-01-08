package se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalanceEntity {
  @JsonProperty("AccountId")
  private String accountId;

  @JsonProperty("CreditCardBalance")
  private CreditBalanceEntity balanceEntity;

  @JsonProperty("DateTime")
  private String dateTime;

  @JsonProperty("Active")
  private boolean active;

  public String getAccountId() {
    return accountId;
  }

  public CreditBalanceEntity getBalanceEntity() {
    return balanceEntity;
  }

  public String getDateTime() {
    return dateTime;
  }

  public boolean isActive() {
    return active;
  }
}
