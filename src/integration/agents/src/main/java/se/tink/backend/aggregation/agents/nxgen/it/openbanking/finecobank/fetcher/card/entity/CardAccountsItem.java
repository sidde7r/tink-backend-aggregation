package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.card.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.Formats;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class CardAccountsItem {

    private String resourceId;
    private String product;
    private List<BalancesEntity> balances;
    private String maskedPan;

    @JsonProperty("_links")
    private LinksEntity links;

    private String name;
    private String currency;
    private CreditLimitEntity creditLimit;

    public String getResourceId() {
        return resourceId;
    }

    public String getProduct() {
        return product;
    }

    public List<BalancesEntity> getBalances() {
        return balances;
    }

    public String getMaskedPan() {
        return maskedPan;
    }

    public LinksEntity getLinks() {
        return links;
    }

    public String getName() {
        return name;
    }

    public String getCurrency() {
        return currency;
    }

    public CreditLimitEntity getCreditLimit() {
        return creditLimit;
    }

    @JsonIgnore
    public CreditCardAccount toCreditCardAccount() {

        return CreditCardAccount.nxBuilder()
                .withCardDetails(
                        CreditCardModule.builder()
                                .withCardNumber(maskedPan)
                                .withBalance(getBalance())
                                .withAvailableCredit(creditLimit.toTinkAmount())
                                .withCardAlias(name)
                                .build())
                .withPaymentAccountFlag()
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(maskedPan)
                                .withAccountNumber(maskedPan)
                                .withAccountName(product)
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                AccountIdentifierType.PAYMENT_CARD_NUMBER,
                                                maskedPan))
                                .build())
                .setApiIdentifier(resourceId)
                .build();
    }

    @JsonIgnore
    private ExactCurrencyAmount getBalance() {
        return balances.stream()
                .filter(BalancesEntity::isForwardBalanceAvailable)
                .findAny()
                .map(balanceEntity -> balanceEntity.getBalanceAmount().toTinkAmount())
                .orElseGet(this::getInterimBalance);
    }

    @JsonIgnore
    private ExactCurrencyAmount getInterimBalance() {
        return balances.stream()
                .filter(BalancesEntity::isInterimBalanceAvailable)
                .findAny()
                .map(balanceEntity -> balanceEntity.getBalanceAmount().toTinkAmount())
                .orElse(new ExactCurrencyAmount(BigDecimal.ZERO, Formats.CURRENCY));
    }
}
