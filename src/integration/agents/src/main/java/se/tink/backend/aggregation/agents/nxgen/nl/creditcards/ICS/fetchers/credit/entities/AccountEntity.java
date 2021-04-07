package se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.rpc.CreditBalanceResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;

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
        final String customerNumber = creditCardEntity.getCustomerNumber();

        // CC data from the bank do not returned any CardNumber or Alias
        return CreditCardAccount.nxBuilder()
                .withCardDetails(
                        CreditCardModule.builder()
                                .withCardNumber(customerNumber)
                                .withBalance(balanceResponse.toTinkBalanceAmount(accountId))
                                .withAvailableCredit(
                                        balanceResponse.toTinkAvailableCreditAmount(accountId))
                                .withCardAlias(productEntity.getProductName())
                                .build())
                .withInferredAccountFlags()
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(customerNumber)
                                .withAccountNumber(customerNumber)
                                .withAccountName(productEntity.getProductName())
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                AccountIdentifierType.OTHER, customerNumber))
                                .build())
                .setBankIdentifier(accountId)
                .setApiIdentifier(accountId)
                .build();
    }
}
