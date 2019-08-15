package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.StorageKeys;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountEntity {

    @JsonProperty("_links")
    private LinksEntity links;

    private String accountType;
    private String bban;
    private String bic;
    private String cashAccountType;
    private String currency;
    private String iban;
    private String id;
    private String maskedPan;
    private String msisdn;
    private String name;
    private String resourceId;
    private List<BalanceEntity> balances;

    public LinksEntity getLinks() {
        return links;
    }

    @JsonIgnore
    public Optional<TransactionalAccount> toTinkAccount(BalanceEntity balanceEntity) {
        final AccountTypes type =
                Xs2aDevelopersConstants.ACCOUNT_TYPE_MAPPER
                        .translate(accountType)
                        .orElse(AccountTypes.OTHER);

        switch (type) {
            case CHECKING:
                return Optional.ofNullable(
                        toTypeAccount(balanceEntity, TransactionalAccountType.CHECKING));
            case SAVINGS:
                return Optional.ofNullable(
                        toTypeAccount(balanceEntity, TransactionalAccountType.SAVINGS));
            default:
                return Optional.empty();
        }
    }

    @JsonIgnore
    public Optional<CreditCardAccount> toTinkCreditAccount(BalanceEntity balanceEntity) {
        final AccountTypes type =
                Xs2aDevelopersConstants.ACCOUNT_TYPE_MAPPER
                        .translate(accountType)
                        .orElse(AccountTypes.CREDIT_CARD);

        if (type.equals(AccountTypes.CREDIT_CARD)) {
            return Optional.ofNullable(toCreditCardAccount(balanceEntity));
        } else {
            return Optional.empty();
        }
    }

    @JsonIgnore
    private TransactionalAccount toTypeAccount(
            BalanceEntity balanceEntity, TransactionalAccountType transactionalAccountType) {

        String accountName;
        if (!Strings.isNullOrEmpty(name)) {
            accountName = name;
        } else {
            accountName = iban;
        }

        return TransactionalAccount.nxBuilder()
                .withType(transactionalAccountType)
                .withBalance(BalanceModule.of(balanceEntity.toAmount()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(getAccountNumber())
                                .withAccountNumber(getAccountNumber())
                                .withAccountName(getAccountName())
                                .addIdentifier(AccountIdentifier.create(Type.IBAN, iban))
                                .build())
                .addHolderName(getAccountName())
                .setApiIdentifier(resourceId)
                .setBankIdentifier(getAccountNumber())
                .putInTemporaryStorage(StorageKeys.ACCOUNT_ID, resourceId)
                .build();
    }

    @JsonIgnore
    public CreditCardAccount toCreditCardAccount(BalanceEntity balanceEntity) {
        return CreditCardAccount.nxBuilder()
                .withCardDetails(
                        CreditCardModule.builder()
                                .withCardNumber(maskedPan.replace('X', '0'))
                                .withBalance(balanceEntity.toAmount())
                                .withAvailableCredit(balanceEntity.toAmount())
                                .withCardAlias(name)
                                .build())
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(getAccountNumber())
                                .withAccountNumber(getAccountNumber())
                                .withAccountName(getAccountName())
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                Type.PAYMENT_CARD_NUMBER, maskedPan))
                                .build())
                .setApiIdentifier(resourceId)
                .addHolderName(getAccountName())
                .setBankIdentifier(getAccountNumber())
                .putInTemporaryStorage(StorageKeys.ACCOUNT_ID, resourceId)
                .build();
    }

    @JsonIgnore
    private ExactCurrencyAmount getAvailableBalance() {
        return Optional.ofNullable(balances).orElse(Collections.emptyList()).stream()
                .filter(BalanceEntity::isAvailableBalance)
                .map(BalanceEntity::toAmount)
                .findFirst()
                .orElse(BalanceEntity.DEFAULT);
    }

    public String getResourceId() {
        return resourceId;
    }

    private String getAccountNumber() {

        if (!Strings.isNullOrEmpty(iban)) {
            return iban;
        } else if (!Strings.isNullOrEmpty(maskedPan)) {
            return maskedPan;
        }
        return bic;
    }

    private String getAccountName() {
        if (!Strings.isNullOrEmpty(name)) {
            return name;
        } else {
            return accountType;
        }
    }

    public void setBalance(List<BalanceEntity> balances) {
        this.balances = balances;
    }
}
