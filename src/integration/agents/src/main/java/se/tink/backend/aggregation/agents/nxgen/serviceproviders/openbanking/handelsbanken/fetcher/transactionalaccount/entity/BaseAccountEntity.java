package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.StorageKeys;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.CheckingAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.SavingsAccount;
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

    public CheckingAccount createCheckingAccount(BalanceEntity balance) {
        return CheckingAccount.builder()
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
