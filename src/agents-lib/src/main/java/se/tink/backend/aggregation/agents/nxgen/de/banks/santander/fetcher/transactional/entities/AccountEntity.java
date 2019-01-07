package se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.transactional.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountEntity {
  private AccountNumberEntity accountNumber;
  private String accountAlias;
  private AvailableBalance availableBalance;
  private String accountType;
  private String accountNumberSort;

  @JsonProperty("subProducto")
  private SubProductEntity subProductEntity;

  public AccountNumberEntity getAccountNumber() {
    return accountNumber;
  }

  public String getAccountAlias() {
    return accountAlias;
  }

  public AvailableBalance getAvailableBalance() {
    return availableBalance;
  }

  public String getAccountType() {
    return accountType;
  }

  public String getAccountNumberSort() {
    return accountNumberSort;
  }

  public SubProductEntity getSubProductEntity() {
    return subProductEntity;
  }
}
