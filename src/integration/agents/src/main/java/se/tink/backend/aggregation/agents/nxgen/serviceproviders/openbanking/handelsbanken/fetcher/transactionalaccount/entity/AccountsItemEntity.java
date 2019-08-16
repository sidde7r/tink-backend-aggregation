package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants;
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

    public TransactionalAccount toTinkAccount(
            TransactionalAccountType type, BalancesItemEntity balance) {
        return TransactionalAccount.nxBuilder()
                .withType(type)
                .withBalance(BalanceModule.of(getAmount(balance)))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(getAccountNumber())
                                .withAccountNumber(getAccountNumber())
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
                .setBankIdentifier(getAccountNumber())
                .putInTemporaryStorage(HandelsbankenBaseConstants.StorageKeys.ACCOUNT_ID, accountId)
                .build();
    }

    private String getAccountNumber() {
        return iban.substring(iban.length() - 9);
    }

    private ExactCurrencyAmount getAmount(BalancesItemEntity balance) {
        return new ExactCurrencyAmount(
                balance.getAmountEntity().getContent(), balance.getAmountEntity().getCurrency());
    }
}
