package se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditBalanceEntity {
  @JsonProperty("Amount")
  private String amount;

  @JsonProperty("Currency")
  private String currency;

  @JsonProperty("AvailableLimit")
  private String availableLimit;

  @JsonProperty("AuthorizedBalance")
  private String authorizedBalance;

  @JsonProperty("CreditLimit")
  private String creditLimit;

  @JsonProperty("Active")
  private boolean active;

  public String getAmount() {
    return amount;
  }

  public String getCurrency() {
    return currency;
  }

  public String getAvailableLimit() {
    return availableLimit;
  }

  public String getAuthorizedBalance() {
    return authorizedBalance;
  }

  public String getCreditLimit() {
    return creditLimit;
  }

  public boolean isActive() {
    return active;
  }
}
