package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.fetcher.entities;

public class AccountEntity {

  private String accountNumber;
  private String currency;
  private String accountHolderName;

  public String getAccountNumber() {
    return accountNumber;
  }

  public void setAccountNumber(String accountNumber) {
    this.accountNumber = accountNumber;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public String getAccountHolderName() {
    return accountHolderName;
  }

  public void setAccountHolderName(String accountHolderName) {
    this.accountHolderName = accountHolderName;
  }

  public AccountEntity(String accountNumber, String currency, String accountHolderName) {
    this.accountNumber = accountNumber;
    this.currency = currency;
    this.accountHolderName = accountHolderName;
  }
}
