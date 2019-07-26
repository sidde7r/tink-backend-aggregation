package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.fetcher.cardaccounts.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class CardAccountEntity {

    private List<BalanceEntity> balances;
    private CreditLimitEntity creditLimit;
    private String currency;
    private String maskedPan;
    private String name;
    private String product;
    private String resourceId;
    private String status;
    private String usage;

    public List<BalanceEntity> getBalances() {
        return balances;
    }

    public CreditLimitEntity getCreditLimit() {
        return creditLimit;
    }

    public String getCurrency() {
        return currency;
    }

    public String getMaskedPan() {
        return maskedPan;
    }

    public String getName() {
        return name;
    }

    public String getProduct() {
        return product;
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getStatus() {
        return status;
    }

    public String getUsage() {
        return usage;
    }

    @JsonIgnore
    public CreditCardAccount toTinkTransaction() {

        return CreditCardAccount.nxBuilder()
                .withCardDetails(
                        CreditCardModule.builder()
                                .withCardNumber(resourceId)
                                .withBalance(getAvailableBalance())
                                .withAvailableCredit(getAvaliableCredit())
                                .withCardAlias(name)
                                .build())
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(name)
                                .withAccountNumber(name)
                                .withAccountName(product)
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                AccountIdentifier.Type.PAYMENT_CARD_NUMBER,
                                                maskedPan))
                                .build())
                .setApiIdentifier(resourceId)
                .build();
    }

    @JsonIgnore
    private ExactCurrencyAmount getAvaliableCredit() {
        return Optional.ofNullable(balances).orElse(Collections.emptyList()).stream()
                .filter(BalanceEntity::isAvailableCredit)
                .findFirst()
                .map(BalanceEntity::toAmount)
                .get();
    }

    @JsonIgnore
    private ExactCurrencyAmount getAvailableBalance() {
        return Optional.ofNullable(balances).orElse(Collections.emptyList()).stream()
                .filter(BalanceEntity::isBalance)
                .findFirst()
                .map(BalanceEntity::toAmount)
                .get();
    }

    @JsonIgnore
    private String createUniqueIdFromMaskedPane() {
        return maskedPan.split("[*]+")[1];
    }
}
