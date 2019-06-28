package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.StorageKeys;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AccountsItem {

    @JsonProperty("accountId")
    private String accountId;

    @JsonProperty("bban")
    private String bban;

    @JsonProperty("ownerName")
    private String ownerName;

    @JsonProperty("_links")
    private Links links;

    @JsonProperty("iban")
    private String iban;

    @JsonProperty("accountType")
    private String accountType;

    @JsonProperty("name")
    private String name;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("bic")
    private String bic;

    @JsonProperty("clearingNumber")
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

    public Links getLinks() {
        return links;
    }

    public String getIban() {
        return iban;
    }

    public String getAccountType() {
        return accountType;
    }

    public String getName() {
        return name;
    }

    public String getCurrency() {
        return currency;
    }

    public String getBic() {
        return bic;
    }

    public String getClearingNumber() {
        return clearingNumber;
    }

    public TransactionalAccount createCheckingAccount(BalancesItem balance) {
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(getAccountNumber())
                                .withAccountNumber(getAccountNumber())
                                .withAccountName(name)
                                .addIdentifier(
                                        AccountIdentifier.create(AccountIdentifier.Type.IBAN, iban))
                                .build())
                .withBalance(BalanceModule.of(getAmount(balance)))
                .addHolderName(ownerName)
                .setApiIdentifier(accountId)
                .setBankIdentifier(getAccountNumber())
                .putInTemporaryStorage(StorageKeys.ACCOUNT_ID, accountId)
                .build();
    }

    private String getAccountNumber() {
        return iban.substring(iban.length() - 9);
    }

    private Amount getAmount(BalancesItem balance) {
        return new Amount(balance.getAmount().getCurrency(), balance.getAmount().getContent());
    }
}
