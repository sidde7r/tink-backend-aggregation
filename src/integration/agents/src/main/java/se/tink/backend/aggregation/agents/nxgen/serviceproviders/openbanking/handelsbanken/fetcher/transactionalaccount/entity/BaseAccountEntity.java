package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.StorageKeys;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.SavingsAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;

@JsonObject
public class BaseAccountEntity {

    private String accountId;
    private String iban;
    private String bban;
    private String currency;
    private String accountType;
    private String bic;
    private String clearingNumber;
    private Double creditLimit;
    private String name;
    private String ownerName;

    public String getAccountId() {
        return accountId;
    }

    public String getAccountType() {
        return accountType;
    }

    public TransactionalAccount createCheckingAccount(BalanceEntity balance) {
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
        return iban.substring(9);
    }

    private Amount getAmount(BalanceEntity balance) {
        return new Amount(balance.getAmount().getCurrency(), balance.getAmount().getContent());
    }

    public SavingsAccount createSavingsAccount(BalanceEntity balance) {
        return SavingsAccount.builder()
                .setUniqueIdentifier(iban)
                .setAccountNumber(iban)
                .setBalance(
                        new Amount(
                                balance.getAmount().getCurrency(),
                                balance.getAmount().getContent()))
                .setAlias(name)
                .addAccountIdentifier(AccountIdentifier.create(AccountIdentifier.Type.IBAN, iban))
                .addHolderName(ownerName)
                .putInTemporaryStorage(StorageKeys.ACCOUNT_ID, accountId)
                .build();
    }
}
