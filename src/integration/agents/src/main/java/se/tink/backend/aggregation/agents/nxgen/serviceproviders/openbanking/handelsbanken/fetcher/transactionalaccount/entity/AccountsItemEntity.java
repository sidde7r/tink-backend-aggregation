package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountsItemEntity {

    private String accountId;
    private String bban;
    private String ownerName;

    @JsonProperty("_links")
    private LinksEntity linksEntity;

    private String iban;
    private String accountType;
    private String name;
    private String currency;
    private String bic;
    private String clearingNumber;

    public String getAccountId() {
        return accountId;
    }

    public String getBban() {
        return bban;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public LinksEntity getLinksEntity() {
        return linksEntity;
    }

    public String getIban() {
        return iban;
    }

    public String getAccountType() {
        return accountType;
    }

    public Optional<TransactionalAccount> toTinkAccount(
            TransactionalAccountType type, BalancesItemEntity balance) {
        return TransactionalAccount.nxBuilder()
                .withType(type)
                .withPaymentAccountFlag()
                .withBalance(BalanceModule.of(getAmount(balance)))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(getAccountNumberWithoutClearing())
                                .withAccountNumber(getAccountNumberWithClearing())
                                .withAccountName(Optional.ofNullable(name).orElse(accountType))
                                .addIdentifier(
                                        AccountIdentifier.create(AccountIdentifier.Type.IBAN, iban))
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                AccountIdentifier.Type.SE,
                                                clearingNumber.concat(bban)))
                                .build())
                .addHolderName(ownerName)
                .setApiIdentifier(accountId)
                .build();
    }

    /**
     * Parsing the iban instead of using the bban directly as they may suddenly start including the
     * clearing number in the bban. As we're using the account number without clearing as unique
     * identifier it's not a risk we want to take.
     */
    @JsonIgnore
    private String getAccountNumberWithoutClearing() {
        return iban.substring(iban.length() - 9);
    }

    @JsonIgnore
    private String getAccountNumberWithClearing() {
        if (Strings.isNullOrEmpty(clearingNumber)) {
            return getAccountNumberWithoutClearing();
        }

        return clearingNumber + "-" + getAccountNumberWithoutClearing();
    }

    @JsonIgnore
    private ExactCurrencyAmount getAmount(BalancesItemEntity balance) {
        return new ExactCurrencyAmount(
                balance.getAmountEntity().getContent(), balance.getAmountEntity().getCurrency());
    }
}
