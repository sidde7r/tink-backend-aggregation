package se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.ICSConstants;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.rpc.CreditBalanceResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

@JsonObject
public class AccountEntity {
  @JsonProperty("AccountId")
  private String accountId;

  @JsonProperty("Currency")
  private String currency;

  @JsonProperty("CreditCardAccountInfo")
  private CreditCardEntity creditCardEntity;

  @JsonProperty("ProductInfo")
  private ProductEntity productEntity;

  public String getAccountId() {
    return accountId;
  }

  public String getCurrency() {
    return currency;
  }

  public CreditCardEntity getCreditCardEntity() {
    return creditCardEntity;
  }

  public ProductEntity getProductEntity() {
    return productEntity;
  }

  public CreditCardAccount toCreditCardAccount(CreditBalanceResponse balanceResponse) {
    return CreditCardAccount.builder(
            accountId,
            balanceResponse.getBalance(accountId),
            balanceResponse.getAvailableCredit(accountId))
        .setName(productEntity.getProductName())
        .setAccountNumber(creditCardEntity.getCustomerNumber())
        .putInTemporaryStorage(ICSConstants.Storage.ACCOUNT_ID, accountId)
        .build();
  }
}
