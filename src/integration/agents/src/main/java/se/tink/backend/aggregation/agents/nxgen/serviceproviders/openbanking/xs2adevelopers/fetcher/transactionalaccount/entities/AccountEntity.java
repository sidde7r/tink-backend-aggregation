package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.ObjectUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.StorageKeys;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;

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
    private String product;
    private List<BalanceEntity> balances;

    public LinksEntity getLinks() {
        return links;
    }

    public Optional<TransactionalAccount> toTinkAccount(BalanceEntity balanceEntity) {
        if (iban == null) {
            return Optional.empty();
        }

        return TransactionalAccount.nxBuilder()
                .withTypeAndFlagsFrom(Xs2aDevelopersConstants.ACCOUNT_TYPE_MAPPER, getAccountType())
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

    public boolean isCreditCardAccount() {
        return !Strings.isNullOrEmpty(maskedPan);
    }

    public CreditCardAccount toTinkCreditAccount(BalanceEntity balanceEntity) {
        return CreditCardAccount.nxBuilder()
                .withCardDetails(
                        CreditCardModule.builder()
                                .withCardNumber(maskedPan)
                                .withBalance(balanceEntity.toAmount())
                                .withAvailableCredit(balanceEntity.toAmount())
                                .withCardAlias(name)
                                .build())
                .withPaymentAccountFlag()
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
        return Strings.nullToEmpty(!Strings.isNullOrEmpty(name) ? name : getAccountType());
    }

    private String getAccountType() {
        return ObjectUtils.firstNonNull(accountType, cashAccountType, product);
    }

    public void setBalance(List<BalanceEntity> balances) {
        this.balances = balances;
    }
}
